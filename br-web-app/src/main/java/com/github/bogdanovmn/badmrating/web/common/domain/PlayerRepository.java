package com.github.bogdanovmn.badmrating.web.common.domain;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlayerRepository {
    private static final RowMapper<PlayerSearchResult> PLAYER_SEARCH_RESULT_ROW_MAPPER = (rs, rowNum)
        -> PlayerSearchResult.builder()
            .id(UUID.fromString(rs.getString("id")))
            .importId(rs.getLong("import_id"))
            .details(
                Player.builder()
                    .name(rs.getString("name"))
                    .year(rs.getObject("year") != null ? rs.getInt("year") : null)
                    .region(rs.getString("region"))
                    .rank(PlayerRank.valueOf(rs.getString("rank")))
                .build()
            ).build();

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;

    public List<PlayerSearchResult> findByTerm(String term) {
        String preparedTerm = Arrays.stream(
            term.replaceAll("[^\\p{L}\\s]", "")
                .split("\\s+")
            ).filter(word -> !word.isEmpty())
            .map(word -> word + ":*")
            .collect(joining(" & "));

        return jdbc.query("""
            SELECT p.id, p.name, p.year, r.short_name region, p.rank, p.import_id
            FROM player p
            JOIN region r ON r.id = p.region_id
            ORDER BY LOWER(p.name) <-> LOWER(:term), p.name, p.year
            LIMIT 15
            """,
            Map.of("term", preparedTerm),
            PLAYER_SEARCH_RESULT_ROW_MAPPER
        );
    }

    public Optional<PlayerSearchResult> find(Player player) {
        return jdbc.query("""
            SELECT p.id, p.name, p.year, r.short_name region, p.rank, p.import_id
            FROM player p
            LEFT JOIN region r ON r.id = p.region_id
            WHERE p.name = :name
            """,
            Map.of("name", player.getName()),
            PLAYER_SEARCH_RESULT_ROW_MAPPER
        ).stream().filter(
            p -> p.getDetails().equals(player)
        ).findFirst();
    }

    public PlayerSearchResult create(Long importId, Player player) {
        return jdbc.queryForObject("""
            INSERT INTO player (import_id, name, year, region_id, rank)
            VALUES(:importId, :name, :year, :regionId, :rank)
            RETURNING id
            """,
            new MapSqlParameterSource()
                .addValue("importId", importId)
                .addValue("name", player.getName())
                .addValue("year", player.getYear())
                .addValue("regionId", getRegionId(player.getRegion()))
                .addValue("rank", player.getRank().toString()),

            (rs, rowNum) -> PlayerSearchResult.builder()
                .id(UUID.fromString(rs.getString("id")))
                .details(player)
            .build()
        );
    }

    private Long getRegionId(String region) {
        return region == null
            ? null
            : jdbc.queryForObject("""
                INSERT INTO region (short_name) VALUES (:region)
                ON CONFLICT (short_name) DO UPDATE SET short_name = EXCLUDED.short_name
                RETURNING id
                """,
                Map.of("region", region),
                Long.class
            );
    }

    public void update(Long importId, UUID playerId, Player details) {
        jdbc.update("""
            UPDATE player SET
                import_id = :importId,
                name = :name,
                year = :year,
                region_id = :regionId,
                rank = :rank
            WHERE id = :playerId
            """,
            new MapSqlParameterSource()
                .addValue("importId", importId)
                .addValue("playerId", playerId)
                .addValue("name", details.getName())
                .addValue("year", details.getYear())
                .addValue("regionId", getRegionId(details.getRegion()))
                .addValue("rank", details.getRank().toString())
        );
    }

    public void addRatingsBulk(Long importId, Map<UUID, List<PersonalRating>> playerRatings) {
        List<String> valueClauses = new ArrayList<>(playerRatings.size());
        List<Object> params = new ArrayList<>(playerRatings.size() * 2);

        playerRatings.forEach((playerId, ratings) -> {
            ratings.forEach(rating -> {
                valueClauses.add("(?, ?, ?, ?)");
                params.add(playerId);
                params.add(rating.getType().toString());
                params.add(importId);
                params.add(rating.getValue());
            });
        });

        jdbcTemplate.update(
            String.format(
                "INSERT INTO rating (player_id, play_type, import_id, value) VALUES %s",
                    String.join(",", valueClauses)
            ),
            params.toArray(new Object[0])
        );
    }

    public void savePreviousDetails(PlayerSearchResult persisted) {
        jdbc.update("""
            INSERT INTO player_previous_details (import_id, player_id, year, region_id, rank)
            VALUES (
                :importId,
                :playerId,
                :year,
                (SELECT id FROM region WHERE short_name = :regionShortName),
                :rank
            )
            """,
            new MapSqlParameterSource()
                .addValue("importId", persisted.getImportId())
                .addValue("playerId", persisted.getId())
                .addValue("year", persisted.getDetails().getYear())
                .addValue("regionShortName", persisted.getDetails().getRegion())
                .addValue("rank", persisted.getDetails().getRank().toString())
        );
    }
}
