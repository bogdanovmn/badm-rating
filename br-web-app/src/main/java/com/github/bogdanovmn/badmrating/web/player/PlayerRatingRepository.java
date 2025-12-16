package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRatingSnapshot;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.common.domain.TopType;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.bogdanovmn.badmrating.web.common.domain.TopType.global;

@Component
@RequiredArgsConstructor
class PlayerRatingRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<PlayerRatingQueryResultRow> playerRatingHistory(UUID playerId) {
        return jdbc.query("""
            WITH ranked_ratings AS (
                SELECT
                    r.play_type,
                    i.file_date,
                    i.source_id,
                    r.value,
                    LAG(r.value) OVER (PARTITION BY r.play_type, i.source_id ORDER BY i.file_date) AS prev_value,
                    ROW_NUMBER() OVER (PARTITION BY r.play_type, i.source_id ORDER BY i.file_date) AS rn,
                    COUNT(*) OVER (PARTITION BY r.play_type, i.source_id) AS total_count
                FROM rating r
                JOIN import i ON r.import_id = i.id
                WHERE r.player_id = :playerId
            )
            SELECT
                play_type,
                file_date,
                source_id,
                value
            FROM ranked_ratings
            WHERE
                rn = 1
                OR rn = total_count
                OR value != prev_value
            """,
            Map.of("playerId", playerId),
            (rs, rowNum) -> PlayerRatingQueryResultRow.builder()
                .value(rs.getInt("value"))
                .playType(PlayType.valueOf(rs.getString("play_type")))
                .date(rs.getTimestamp("file_date").toLocalDateTime().toLocalDate())
                .source(Source.byId(rs.getInt("source_id")))
            .build()
        );
    }

    List<PlayerRatingState> playerRatingState(UUID playerId, Source source, TopType topType) {
        return jdbc.query("""
            WITH last_import AS (
                SELECT id, file_date
                FROM import
                WHERE source_id = :sourceId
                ORDER BY file_date DESC
                LIMIT 1
            )
            SELECT
                t.play_type,
                %s rating_date,
                t.position,
                t.rating_value,
                t.rating_change,
                t.position_change
            FROM player_%s_top_position t
            JOIN last_import li ON li.id = t.import_id
            JOIN player p ON p.id = t.player_id
            LEFT JOIN region r ON r.id = p.region_id
            WHERE t.source_id = (SELECT source_id FROM last_import)
              AND p.id = :playerId
            ORDER BY t.position, p.name, p.id
            """.formatted(
                topType == global ? "t.rating_date" : "li.file_date",
                topType.name()
            ),
            Map.of(
                "sourceId", source.getId(),
                "playerId", playerId
            ),
            (rs, rowNum) -> PlayerRatingState.builder()
                .playType(PlayType.valueOf(rs.getString("play_type")))
                .source(source)
                .ratingSnapshot(
                    PlayerRatingSnapshot.builder()
                        .position(rs.getInt("position"))
                        .rating(rs.getInt("rating_value"))
                        .ratingChange(rs.getInt("rating_change"))
                        .positionChange(rs.getInt("position_change"))
                        .updatedAt(rs.getTimestamp("rating_date").toLocalDateTime().toLocalDate())
                    .build()
                )
            .build()
        );
    }

    @Value
    @Builder
    static class PlayerRatingQueryResultRow {
        int value;
        PlayType playType;
        LocalDate date;
        Source source;
    }
}
