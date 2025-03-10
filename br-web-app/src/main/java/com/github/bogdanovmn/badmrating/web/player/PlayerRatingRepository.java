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
        return jdbc.query("""
            SELECT r.play_type, i.file_date, r.value
            FROM rating r
            JOIN import i ON r.import_id = i.id
            WHERE r.player_id = :playerId
            ORDER BY i.file_date
            """,
            Map.of("playerId", playerId),
            (rs, rowNum) -> PlayerRating.builder()
                .value(rs.getInt("value"))
                .playType(PlayType.valueOf(rs.getString("play_type")))
                .date(rs.getTimestamp("file_date").toLocalDateTime().toLocalDate())
            .build()
        );
    }


}
