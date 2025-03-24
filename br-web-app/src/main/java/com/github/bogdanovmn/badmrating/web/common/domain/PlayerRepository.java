package com.github.bogdanovmn.badmrating.web.common.domain;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
public class PlayerRepository {
    private static final RowMapper<PlayerSearchResult> PLAYER_SEARCH_RESULT_ROW_MAPPER = (rs, rowNum)
        -> PlayerSearchResult.builder()
            .id(UUID.fromString(rs.getString("id")))
            .details(
                Player.builder()
                    .name(rs.getString("name"))
                    .year(rs.getInt("year"))
                    .region(rs.getString("region"))
                    .rank(PlayerRank.valueOf(rs.getString("rank")))
                .build()
            ).build();

    private final NamedParameterJdbcTemplate jdbc;

    public List<PlayerSearchResult> findByTerm(String term) {
        String preparedTerm = Arrays.stream(
            term.replaceAll("[^\\p{L}\\s]", "")
                .split("\\s+")
            ).filter(word -> !word.isEmpty())
            .map(word -> word + ":*")
            .collect(joining(" & "));

        return jdbc.query("""
            SELECT p.id, p.name, p.year, r.short_name region, p.rank
            FROM player p
            JOIN region r ON r.id = p.region_id
            WHERE p.name_fts @@ to_tsquery('russian', :term)
            """,
            Map.of("term", preparedTerm),
            PLAYER_SEARCH_RESULT_ROW_MAPPER
        );
    }

    public PlayerSearchResult find(Player player) {
        List<PlayerSearchResult> result = jdbc.query("""
            SELECT p.id, p.name, p.year, r.short_name region, p.rank
            FROM player p
            LEFT JOIN region r ON r.id = p.region_id
            WHERE p.name = :name
            """,
            Map.of("name", player.getName()),
            PLAYER_SEARCH_RESULT_ROW_MAPPER
        );
        return result.isEmpty() ? null : result.get(0);
    }

    public PlayerSearchResult create(Player player) {
        return jdbc.queryForObject("""
            INSERT INTO player (name, year, region_id, rank)
            VALUES(:name, :year, :regionId, :rank)
            RETURNING id
            """,
            new MapSqlParameterSource()
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

    public void update(UUID playerId, Player details) {
        jdbc.update("""
            UPDATE player SET
                name = :name,
                year = :year,
                region_id = :regionId,
                rank = :rank
            WHERE id = :playerId
            """,
            new MapSqlParameterSource()
                .addValue("playerId", playerId)
                .addValue("name", details.getName())
                .addValue("year", details.getYear())
                .addValue("regionId", getRegionId(details.getRegion()))
                .addValue("rank", details.getRank().toString())
        );
    }

    public void addRating(Long importId, UUID playerId, List<PersonalRating> personalRatings) {
        jdbc.batchUpdate("""
            INSERT INTO rating (player_id, play_type, import_id, value)
            VALUES (:playerId, :playType, :importId, :value)
            """,
            personalRatings.stream()
                .map(rating -> new MapSqlParameterSource()
                    .addValue("playerId", playerId)
                    .addValue("playType", rating.getType().toString())
                    .addValue("importId", importId)
                    .addValue("value", rating.getValue())
                )
                .toArray(SqlParameterSource[]::new)
        );
    }
}
