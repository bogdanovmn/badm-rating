package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Value
    @Builder
    static class PlayerRatingQueryResultRow {
        int value;
        PlayType playType;
        LocalDate date;
        Source source;
    }
}
