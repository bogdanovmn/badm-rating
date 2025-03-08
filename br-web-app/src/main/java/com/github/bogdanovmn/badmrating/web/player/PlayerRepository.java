package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Component
@RequiredArgsConstructor
class PlayerRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<PlayerSearchResult> findByTerm(String term) {
        String preparedTerm = Arrays.stream(
            term.replaceAll("[^\\p{L}\\s]", "")
                .split("\\s+")
            ).filter(word -> !word.isEmpty())
            .map(word -> word + ":*")
            .collect(joining(" & "));

        return jdbc.query("""
            SELECT id, name, year, r.short_name region
            FROM players p
            JOIN region r ON r.id = p.region_id
            WHERE p.name_fts @@ to_tsquery('russian', :term)
        """,
            Map.of("term", preparedTerm),
            (rs, rowNum) -> PlayerSearchResult.builder()
                .id(UUID.fromString(rs.getString("id")))
                .details(
                    Player.builder()
                        .name(rs.getString("name"))
                        .year(rs.getInt("year"))
                        .region(rs.getString("region"))
                    .build()
                ).build()
            );
    }
}
