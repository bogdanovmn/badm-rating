package com.github.bogdanovmn.badmrating.web.statistic;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.statistic.TopStatistic.PlayerPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
class PlayersStatisticService {
    private final PlayersStatisticRepository playersStatisticRepository;

    private static final Set<String> SOURCES = Set.of("RNBF", "RNBFJunior");

    @Value("${statistic.players.top.max-results:30}")
    private int topMaxResults;

    List<TopStatistic> getPlayersTop() {
        List<TopStatistic> result = new ArrayList<>(10);
        for (String source: SOURCES) {
            for (PlayType playType: PlayType.values()) {
                if (playType != PlayType.UNKNOWN) {
                    result.add(
                        TopStatistic.builder()
                            .source(source)
                            .type(playType)
                            .data(
                                playersStatisticRepository.getPlayersTop(source, playType, topMaxResults)
                                    .stream().map(r ->
                                        PlayerPosition.builder()
                                            .player(r.getPlayer())
                                            .position(r.getPosition())
                                            .rating(r.getValue())
                                            .updatedAt(r.getUpdatedAt())
                                        .build()
                                    ).toList()
                            )
                        .build()
                    );
                }
            }
        }
        return result;
    }
}
