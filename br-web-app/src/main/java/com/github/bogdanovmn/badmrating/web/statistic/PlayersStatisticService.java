package com.github.bogdanovmn.badmrating.web.statistic;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.statistic.PlayersStatisticRepository.PlayersTopQueryResultRow;
import com.github.bogdanovmn.badmrating.web.statistic.TopStatistic.PlayerPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
class PlayersStatisticService {
    private final PlayersStatisticRepository playersStatisticRepository;

    private static final Set<String> SOURCES = Set.of("RNBF", "RNBFJunior");

    @Value("${statistic.players.top.max-results:50}")
    private int topMaxResults;

    record TopQueryParams(String source, PlayType playType, Integer maxResults) {}

    private List<TopStatistic> getPlayersTopByFunction(
        Function<TopQueryParams, List<PlayersTopQueryResultRow>> repositoryFunction
    ) {
        List<TopStatistic> result = new ArrayList<>(10);
        for (String source : SOURCES) {
            for (PlayType playType : PlayType.values()) {
                if (playType != PlayType.UNKNOWN) {
                    TopQueryParams params = new TopQueryParams(source, playType, topMaxResults);
                    result.add(
                        TopStatistic.builder()
                            .source(source)
                            .type(playType)
                            .data(
                                repositoryFunction.apply(params)
                                    .stream()
                                    .map(r ->
                                        PlayerPosition.builder()
                                            .player(r.getPlayer())
                                            .position(r.getPosition())
                                            .rating(r.getValue())
                                            .updatedAt(r.getUpdatedAt())
                                            .build()
                                    )
                                    .toList()
                            )
                        .build()
                    );
                }
            }
        }
        return result;
    }

    public List<TopStatistic> getPlayersTop() {
        return getPlayersTopByFunction(
            params -> playersStatisticRepository.getPlayersGlobalTop(
                params.source(), params.playType(), params.maxResults()
            )
        );
    }

    public List<TopStatistic> getPlayersActualTop() {
        return getPlayersTopByFunction(
            params -> playersStatisticRepository.getPlayersActualTop(
                params.source(), params.playType(), params.maxResults()
            )
        );
    }
}
