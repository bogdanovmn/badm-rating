package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.player.PlayerRatingRepository.PlayerRatingQueryResultRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;

    private record SourcePlayTypeKey(String source, PlayType playType) {}

    List<PlayerRating> playerRatingHistory(UUID playerId) {
        return playerRatingRepository.playerRatingHistory(playerId)
            .stream().collect(Collectors.groupingBy(
                row -> new SourcePlayTypeKey(row.getSource(), row.getPlayType()),
                Collectors.toMap(
                    PlayerRatingQueryResultRow::getDate,
                    PlayerRatingQueryResultRow::getValue
                )
            ))
            .entrySet().stream()
            .map(entry -> PlayerRating.builder()
                .source(entry.getKey().source)
                .playType(entry.getKey().playType)
                .data(entry.getValue())
            .build())
            .collect(Collectors.toList());
    }

}
