package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.common.domain.TopType;
import com.github.bogdanovmn.badmrating.web.player.PlayerRatingRepository.PlayerRatingQueryResultRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
class PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;

    List<PlayerRatingState> ratingState(UUID playerId, TopType topType) {
        return Stream.concat(
            playerRatingRepository.playerRatingState(playerId, Source.RNBFJunior, topType).stream(),
            playerRatingRepository.playerRatingState(playerId, Source.RNBF, topType).stream()
        ).toList();
    }

    private record SourcePlayTypeKey(Source source, PlayType playType) {}

    List<PlayerRatingHistory> ratingHistory(UUID playerId) {
        return playerRatingRepository.playerRatingHistory(playerId)
            .stream().collect(Collectors.groupingBy(
                row -> new SourcePlayTypeKey(row.getSource(), row.getPlayType()),
                Collectors.toMap(
                    PlayerRatingQueryResultRow::getDate,
                    PlayerRatingQueryResultRow::getValue,
                    // в одном файле может быть указано несколько значений рейтинга, нам нужны самые актуальные
                    // TODO: надо перенести эту логику на процесс импорта
                    Math::max
                )
            ))
            .entrySet().stream()
            .map(entry -> PlayerRatingHistory.builder()
                .source(entry.getKey().source)
                .playType(entry.getKey().playType)
                .data(entry.getValue())
            .build())
            .collect(Collectors.toList());
    }

}
