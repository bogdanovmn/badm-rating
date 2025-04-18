package com.github.bogdanovmn.badmrating.web.statistic;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class PlayersStatisticRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<PlayersTopQueryResultRow> getPlayersTop(String source, PlayType playType, int topMaxResults) {
        return jdbc.query("""
                WITH actual_rating AS (
                   SELECT DISTINCT ON (p.id)
                      p.id player_id,
                      r.value,
                      i.id import_id
                  FROM rating r
                  JOIN import i ON r.import_id = i.id
                  JOIN player p ON r.player_id = p.id
                  WHERE r.play_type = :playType
                  AND i.source = :source
                  ORDER BY p.id, i.file_date DESC
                )
                SELECT
                    p.id,
                    p.name,
                    p.year,
                    r.short_name region,
                    p.rank,
                    i.file_date,
                    a.import_id,
                    a.value
                FROM player p
                JOIN actual_rating a ON a.player_id = p.id
                JOIN import i ON i.id = a.import_id
                LEFT JOIN region r ON r.id = p.region_id
                ORDER BY a.value DESC
                LIMIT :limit
                """,
            Map.of(
                "source", source,
                "playType", playType.toString(),
                "limit", topMaxResults
            ),
            (rs, rowNum) -> PlayersTopQueryResultRow.builder()
                .player(
                    PlayerRepository.PLAYER_SEARCH_RESULT_ROW_MAPPER.mapRow(rs, rowNum)
                )
                .position(rowNum + 1)
                .value(rs.getInt("value"))
                .updatedAt(rs.getTimestamp("file_date").toLocalDateTime().toLocalDate())
            .build()
        );
    }

    @Value
    @Builder
    static class PlayersTopQueryResultRow {
        PlayerSearchResult player;
        int position;
        int value;
        LocalDate updatedAt;
    }


}
