package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import com.github.bogdanovmn.badmrating.web.common.domain.TopType;
import com.github.bogdanovmn.badmrating.web.top.TopPlayersRepository.TopPositionHistoryRow;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class TopPlayersService {
    private final TopPlayersRepository topPlayersRepository;

    @Value("${top-players.size:50}")
    private int topSize;
    @Value("${top-players.context.size:3}")
    private int topContextSize;

    public List<PlayerTopPosition> playersTop(Source source, PlayType playType, TopType topType) {
        return sortedPositions(
            topPlayersRepository.playersTop(topType, source, playType, topSize)
        );
    }

    public List<PlayerTopPosition> playerTopContext(UUID playerId, Source source, PlayType playType, TopType topType) {
        return sortedPositions(
            topPlayersRepository.topPositionContext(topType, playerId, source, playType, topContextSize)
        );
    }

    private List<PlayerTopPosition> sortedPositions(List<PlayerTopPosition> positions) {
        return positions.stream()
            .sorted(
                Comparator.comparingInt((PlayerTopPosition p) -> p.getRatingSnapshot().getPosition())
                    .thenComparing(pp -> pp.getPlayer().getDetails().getName())
                    .thenComparing(pp -> pp.getPlayer().getId())
            ).toList();

    }

    Map<LocalDate, Integer> playerTopPositionHistory(UUID playerId, Source source, PlayType playType, TopType topType) {
        return topPlayersRepository.topPositionHistory(topType, playerId, source, playType).stream()
            .collect(Collectors.toMap(TopPositionHistoryRow::positionDate, TopPositionHistoryRow::position));
    }
}
