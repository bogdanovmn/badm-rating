package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.top.TopPlayersController.TopType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
              AND t.source_id = (SELECT source_id FROM last_import)
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
                  AND t.source_id = (SELECT source_id FROM last_import)
            ),
            base_players AS (
                SELECT
                    t.player_id,
                    t.import_id,
                    %s AS rating_date,
                    t.rating_value,
                    t.position,
                    t.rating_change,
                    t.position_change,
                    ABS(t.position - (SELECT position FROM player_position)) AS position_diff
                FROM player_%s_top_position t
                JOIN last_import li ON li.id = t.import_id
                WHERE t.play_type = :playType
                  AND EXISTS (SELECT 1 FROM player_position)
                  AND t.source_id = (SELECT source_id FROM last_import)
            ),
            ranked_players AS (
                SELECT *,
                    ROW_NUMBER() OVER (
                        ORDER BY
                            CASE WHEN player_id = :playerId THEN 0 ELSE 1 END,  -- Сначала целевой игрок
                            position_diff,
                            position_change DESC
                    ) AS global_rank
                FROM base_players
                WHERE
                    -- Либо это наш игрок, либо попадает в топ-12 ближайших
                    player_id = :playerId OR
                    (player_id != :playerId AND position_diff <= :contextSize)
            )
            SELECT
                p.id,
                p.name,
                p.year,
                r.short_name AS region,
                p.rank,
                p.import_id,
                rp.rating_date,
                rp.rating_value,
                rp.position,
                rp.rating_change,
                rp.position_change
            FROM ranked_players rp
            JOIN player p ON p.id = rp.player_id
            LEFT JOIN region r ON r.id = p.region_id
            WHERE rp.global_rank <= 1 + :contextSize * 3
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
              SELECT id, source_id, file_date
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
              SELECT
                  t.player_id,
                  t.play_type,
                  t.position,
                  t.rating_value
              FROM player_actual_top_position t
              JOIN prev_import pi ON t.import_id = pi.id
              WHERE t.source_id = (SELECT source_id FROM current_import)
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
                  mr.play_type,
                  DENSE_RANK() OVER (PARTITION BY mr.play_type ORDER BY mr.value DESC) AS position,
                  mr.value
              FROM max_ratings mr
            )
            INSERT INTO player_actual_top_position (source_id, player_id, import_id, play_type, position, rating_value, rating_change, position_change)
            SELECT
              (SELECT source_id FROM current_import),
              rp.player_id,
              (SELECT id FROM current_import),
              rp.play_type,
              rp.position,
              rp.value,
              CASE
                  WHEN pp.rating_value IS NULL THEN 0
                  ELSE rp.value - pp.rating_value
              END rating_change,
            
              CASE
                  WHEN pp.position IS NULL THEN 0
                  ELSE pp.position - rp.position
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
              SELECT id, source_id, file_date
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
            prev_top AS (
                SELECT
                    t.player_id,
                    t.play_type,
                    t.position,
                    t.rating_value,
                    t.rating_date
                FROM player_global_top_position t
                WHERE t.source_id = (SELECT source_id FROM current_import)
                AND t.import_id = (SELECT id FROM prev_import)
            ),
            latest_ratings AS (
              SELECT DISTINCT ON (r.player_id, r.play_type)
                  r.player_id,
                  r.play_type,
                  r.value,
                  i.file_date
              FROM rating r
              JOIN import i ON r.import_id = i.id
              WHERE i.source_id = (SELECT source_id FROM current_import)
              ORDER BY r.player_id, r.play_type, i.file_date DESC, r.value DESC
            ),
            ranked_players AS (
              SELECT
                  lr.player_id,
                  lr.play_type,
                  DENSE_RANK() OVER (PARTITION BY lr.play_type ORDER BY lr.value DESC) AS position,
                  lr.file_date rating_date,
                  lr.value rating_value
              FROM latest_ratings lr
            )
            INSERT INTO player_global_top_position (source_id, player_id, import_id, play_type, position, rating_value, rating_date, rating_change, position_change)
            SELECT
              (SELECT source_id FROM current_import),
              rp.player_id,
              :importId,
              rp.play_type,
              rp.position,
              rp.rating_value,
              rp.rating_date,
 
              CASE
                  WHEN pt.rating_value IS NULL THEN 0
                  ELSE rp.rating_value - pt.rating_value
              END rating_change,
            
              CASE
                  WHEN pt.position IS NULL THEN 0
                  ELSE pt.position - rp.position
              END position_change
            FROM ranked_players rp
            LEFT JOIN prev_top pt ON pt.player_id = rp.player_id AND rp.play_type = pt.play_type
            """,
            Map.of("importId", importId)
        );
    }

    List<TopPositionHistoryRow> topPositionHistory(TopType topType, UUID playerId, Source source, PlayType playType) {
        return jdbc.query("""
            WITH ranked_positions AS (
                SELECT
                    i.file_date AS updated_at,
                    t.position,
                    LAG(t.position) OVER (ORDER BY i.file_date) AS prev_position,
                    ROW_NUMBER() OVER (ORDER BY i.file_date) AS rn,
                    COUNT(*) OVER () AS total_count
                FROM player_%s_top_position t
                JOIN import i ON i.id = t.import_id
                WHERE t.source_id = :sourceId
                  AND t.play_type = :playType
                  AND t.player_id = :playerId
            )
            SELECT
                updated_at,
                position
            FROM ranked_positions
            WHERE rn = 1
            OR rn = total_count
            OR position != prev_position
            """.formatted(
                topType.name()
            ),
            Map.of(
                "playerId", playerId,
                "sourceId", source.getId(),
                "playType", playType.toString()
            ),
            (rs, rowNum) -> new TopPositionHistoryRow(rs.getTimestamp("updated_at").toLocalDateTime().toLocalDate(), rs.getInt("position"))
        );
    }

    record TopPositionHistoryRow(LocalDate positionDate, int position) {}
}
