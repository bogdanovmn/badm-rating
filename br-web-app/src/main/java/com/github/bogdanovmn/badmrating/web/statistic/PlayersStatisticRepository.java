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
public class PlayersStatisticRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<PlayersTopQueryResultRow> getPlayersGlobalTop(String source, PlayType playType, int topMaxResults) {
        return jdbc.query("""
            WITH last_import AS (
                SELECT id
                FROM import
                WHERE source = :source
                ORDER BY file_date DESC
                LIMIT 1
            )
            SELECT
                p.id,
                p.name,
                p.year,
                r.short_name AS region,
                p.rank,
                pgtp.import_id,
                pgtp.rating_date,
                pgtp.position,
                pgtp.rating_value,
                pgtp.play_type,
                pgtp.rating_change,
                pgtp.position_change
            FROM player_global_top_position pgtp
            JOIN last_import li ON li.id = pgtp.import_id
            JOIN player p ON p.id = pgtp.player_id
            LEFT JOIN region r ON r.id = p.region_id
            WHERE pgtp.play_type = :playType
              AND pgtp.position <= :limit
            ORDER BY pgtp.position, p.name, p.id
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
                .position(rs.getInt("position"))
                .value(rs.getInt("rating_value"))
                .valueChange(rs.getInt("rating_change"))
                .positionChange(rs.getInt("position_change"))
                .updatedAt(rs.getTimestamp("rating_date").toLocalDateTime().toLocalDate())
            .build()
        );
    }

    List<PlayersTopQueryResultRow> getPlayersActualTop(String source, PlayType playType, int topMaxResults) {
        return jdbc.query("""
            WITH last_import AS (
                SELECT id, file_date
                FROM import
                WHERE source = :source
                ORDER BY file_date DESC
                LIMIT 1
            )
            SELECT
                p.id,
                p.name,
                p.year,
                r.short_name AS region,
                p.rank,
                li.file_date,
                patp.import_id,
                patp.position,
                patp.rating_value,
                patp.play_type,
                patp.rating_change,
                patp.position_change
            FROM player_actual_top_position patp
            JOIN last_import li ON li.id = patp.import_id
            JOIN player p ON p.id = patp.player_id
            LEFT JOIN region r ON r.id = p.region_id
            WHERE patp.play_type = :playType
            AND patp.position <= :limit
            ORDER BY patp.position, p.name, p.id
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
                .position(rs.getInt("position"))
                .value(rs.getInt("rating_value"))
                .valueChange(rs.getInt("rating_change"))
                .positionChange(rs.getInt("position_change"))
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
        int valueChange;
        int positionChange;
        LocalDate updatedAt;
    }

    public int updatePlayersActualTop(long importId) {
        return jdbc.update("""
            WITH current_import AS (
              SELECT source, file_date
              FROM import
              WHERE id = :importId
            ),
            prev_import AS (
              SELECT i.id, i.file_date
              FROM import i
              JOIN current_import ci ON i.source = ci.source
              WHERE i.file_date < ci.file_date
              ORDER BY i.file_date DESC
              LIMIT 1
            ),
            prev_positions AS (
              SELECT DISTINCT ON (patp.player_id, patp.play_type)
                  patp.player_id,
                  patp.play_type,
                  patp.position,
                  r.value
              FROM player_actual_top_position patp
              JOIN prev_import pi ON patp.import_id = pi.id
              JOIN rating r ON r.player_id = patp.player_id
                  AND r.import_id = patp.import_id
                  AND r.play_type = patp.play_type
              ORDER BY patp.player_id, patp.play_type, r.value DESC
            ),
            max_ratings AS (
              SELECT DISTINCT ON (r.player_id, r.play_type)
                  r.player_id,
                  r.import_id,
                  r.play_type,
                  r.value
              FROM rating r
              WHERE r.import_id = :importId
              ORDER BY r.player_id, r.play_type, r.value DESC
            ),
            ranked_players AS (
              SELECT
                  mr.player_id,
                  mr.import_id,
                  mr.play_type,
                  DENSE_RANK() OVER (PARTITION BY mr.play_type ORDER BY mr.value DESC) AS position,
                  mr.value
              FROM max_ratings mr
            )
            INSERT INTO player_actual_top_position (player_id, import_id, play_type, position, rating_value, rating_change, position_change)
            SELECT
              rp.player_id,
              rp.import_id,
              rp.play_type,
              rp.position,
              rp.value,
              CASE
                  WHEN pp.value IS NULL THEN 0
                  ELSE rp.value - COALESCE(pp.value, 0)
              END rating_change,
            
              CASE
                  WHEN pp.position IS NULL THEN 0
                  ELSE COALESCE(pp.position, 0) - rp.position
              END position_change
            FROM ranked_players rp
            LEFT JOIN prev_positions pp ON pp.player_id = rp.player_id AND rp.play_type = pp.play_type
            """,
            Map.of("importId", importId)
        );
    }

    public int updatePlayersGlobalTop(long importId) {
        return jdbc.update("""
            WITH current_import AS (
              SELECT source, file_date
              FROM import
              WHERE id = :importId
            ),
            prev_import AS (
              SELECT i.id, i.file_date
              FROM import i
              JOIN current_import ci ON i.source = ci.source
              WHERE i.file_date < ci.file_date
              ORDER BY i.file_date DESC
              LIMIT 1
            ),
            prev_positions AS (
                SELECT DISTINCT ON (pgtp.player_id, pgtp.play_type)
                    pgtp.player_id,
                    pgtp.play_type,
                    pgtp.position,
                    r.value
                FROM player_global_top_position pgtp
                JOIN prev_import pi ON pgtp.import_id = pi.id
                JOIN rating r ON
                    r.player_id = pgtp.player_id
                    AND r.import_id = pgtp.import_id
                    AND r.play_type = pgtp.play_type
                ORDER BY pgtp.player_id, pgtp.play_type, r.value DESC
            ),
            latest_ratings AS (
              SELECT DISTINCT ON (r.player_id, r.play_type)
                  r.player_id,
                  r.import_id,
                  r.play_type,
                  r.value,
                  i.file_date
              FROM rating r
              JOIN import i ON i.id = r.import_id
              JOIN current_import ci ON i.source = ci.source
              WHERE i.file_date <= ci.file_date
              ORDER BY r.player_id, r.play_type, i.file_date DESC
            ),
            ranked_players AS (
              SELECT
                  lr.player_id,
                  lr.import_id,
                  lr.play_type,
                  DENSE_RANK() OVER (PARTITION BY lr.play_type ORDER BY lr.value DESC) AS position,
                  lr.value,
                  lr.file_date
              FROM latest_ratings lr
            )
            INSERT INTO player_global_top_position (player_id, import_id, play_type, position, rating_value, rating_date, rating_change, position_change)
            SELECT
              rp.player_id,
              :importId,
              rp.play_type,
              rp.position,
              rp.value,
              rp.file_date,
            
              CASE
                  WHEN pp.value IS NULL THEN 0
                  ELSE rp.value - COALESCE(pp.value, 0)
              END rating_change,
            
              CASE
                  WHEN pp.position IS NULL THEN 0
                  ELSE COALESCE(pp.position, 0) - rp.position
              END position_change
            FROM ranked_players rp
            LEFT JOIN prev_positions pp ON pp.player_id = rp.player_id AND rp.play_type = pp.play_type;
            """,
            Map.of("importId", importId)
        );
    }
}
