package com.github.bogdanovmn.badmrating.web.player;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import com.github.bogdanovmn.badmrating.core.PlayType;


import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PlayerRatingRepository {
    private final NamedParameterJdbcTemplate jdbc;

    List<PlayerRating> playerRatingHistory(UUID playerId) {
        return jdbc.query(
            "SELECT play_type, updated_at, value FROM rating WHERE player_id = :playerId",
            Map.of("playerId", playerId),
            (rs, rowNum) -> PlayerRating.builder()
                .value(rs.getInt("value"))
                .playType(PlayType.valueOf(rs.getString("play_type")))
                .date(rs.getTimestamp("updated_at").toLocalDateTime().toLocalDate())
            .build()
        );
    }


}
