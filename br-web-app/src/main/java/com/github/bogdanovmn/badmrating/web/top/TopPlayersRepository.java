package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.top.TopPlayersController.TopType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.bogdanovmn.badmrating.web.top.TopPlayersController.TopType.global;

@Component
@RequiredArgsConstructor
public class TopPlayersRepository {
    private static final RowMapper<PlayerTopPosition> PLAYERS_TOP_QUERY_RESULT_ROW_MAPPER = (rs, rowNum) -> PlayerTopPosition.builder()
        .player(
            PlayerRepository.PLAYER_SEARCH_RESULT_ROW_MAPPER.mapRow(rs, rowNum)
        )
        .position(rs.getInt("position"))
        .rating(rs.getInt("rating_value"))
        .ratingChange(rs.getInt("rating_change"))
        .positionChange(rs.getInt("position_change"))
        .updatedAt(rs.getTimestamp("rating_date").toLocalDateTime().toLocalDate())
    .build();

    private final NamedParameterJdbcTemplate jdbc;

    List<PlayerTopPosition> playersTop(TopType topType, Source source, PlayType playType, int topMaxResults) {
        return jdbc.query("""
            WITH last_import AS (
                SELECT id, file_date
                FROM import
                WHERE source_id = :sourceId
                ORDER BY file_date DESC
                LIMIT 1
            )
            SELECT
                p.id,
                p.name,
                p.year,
                r.short_name AS region,
                p.rank,
                p.import_id,
                %s rating_date,
                t.position,
                t.rating_value,
                t.play_type,
                t.rating_change,
                t.position_change
            FROM player_%s_top_position t
            JOIN last_import li ON li.id = t.import_id
            JOIN player p ON p.id = t.player_id
            LEFT JOIN region r ON r.id = p.region_id
            WHERE t.play_type = :playType
              AND t.position <= :limit
            ORDER BY t.position, p.name, p.id
            """.formatted(
                topType == global ? "t.rating_date" : "li.file_date",
                topType.name()
            ),
            Map.of(
                "sourceId", source.getId(),
                "playType", playType.toString(),
                "limit", topMaxResults
            ),
            PLAYERS_TOP_QUERY_RESULT_ROW_MAPPER
        );
    }

    List<PlayerTopPosition> topPositionContext(TopType topType, UUID playerId, Source source, PlayType playType, int contextSize) {
        return jdbc.query("""
            WITH last_import AS (
                SELECT id, file_date
                FROM import
                WHERE source_id = :sourceId
                ORDER BY file_date DESC
                LIMIT 1
            ),
            player_position AS (
                SELECT position
                FROM player_%s_top_position t
                JOIN last_import li ON li.id = t.import_id
                WHERE t.player_id = :playerId
                  AND t.play_type = :playType
            ),
            context_players AS (
                SELECT
                    t.player_id,
                    t.import_id,
                    %s rating_date,
                    t.rating_value,
                    t.position,
                    t.rating_change,
                    t.position_change
                FROM player_%s_top_position t
                JOIN last_import li ON li.id = t.import_id
                WHERE t.play_type = :playType
                  AND EXISTS (SELECT 1 FROM player_position)
                  AND t.position BETWEEN
                      (SELECT position FROM player_position) - :contextSize
                      AND (SELECT position FROM player_position) + :contextSize
            )
            SELECT
                p.id,
                p.name,
                p.year,
                r.short_name AS region,
                p.rank,
                p.import_id,
                cp.rating_date,
                cp.rating_value,
                cp.position,
                cp.rating_change,
                cp.position_change
            FROM context_players cp
            JOIN player p ON p.id = cp.player_id
            LEFT JOIN region r ON r.id = p.region_id;
            """.formatted(
                topType.name(),
                topType == global ? "t.rating_date" : "li.file_date",
                topType.name()
            ),
            Map.of(
                "playerId", playerId,
                "sourceId", source.getId(),
                "playType", playType.toString(),
                "contextSize", contextSize
            ),
            PLAYERS_TOP_QUERY_RESULT_ROW_MAPPER
        );
    }

    public int updatePlayersActualTop(long importId) {
        return jdbc.update("""
            WITH current_import AS (
              SELECT source_id, file_date
              FROM import
              WHERE id = :importId
            ),
            prev_import AS (
              SELECT i.id, i.file_date
              FROM import i
              JOIN current_import ci ON i.source_id = ci.source_id
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
            INSERT INTO player_actual_top_position (source_id, player_id, import_id, play_type, position, rating_value, rating_change, position_change)
            SELECT
              (SELECT source_id FROM current_import),
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
              SELECT source_id, file_date
              FROM import
              WHERE id = :importId
            ),
            prev_import AS (
              SELECT i.id, i.file_date
              FROM import i
              JOIN current_import ci ON i.source_id = ci.source_id
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
              JOIN current_import ci ON i.source_id = ci.source_id
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
            INSERT INTO player_global_top_position (source_id, player_id, import_id, play_type, position, rating_value, rating_date, rating_change, position_change)
            SELECT
              (SELECT source_id FROM current_import),
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
