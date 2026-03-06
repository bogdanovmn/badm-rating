package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
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
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.bogdanovmn.badmrating.web.top.YearGroup.ALL;

@Service
@RequiredArgsConstructor
class TopPlayersService {
    private final TopPlayersRepository topPlayersRepository;
    private final PlayerRepository playerRepository;

    @Value("${top-players.size:100}")
    private int topSize;
    @Value("${top-players.context.size:3}")
    private int topContextSize;

    List<PlayerTopPosition> playersTop(Source source, PlayType playType, TopType topType, YearGroup yearGroup) {
        return sortedPositions(
            yearGroup == ALL
                ? topPlayersRepository.playersTop(topType, source, playType, topSize)
                : topPlayersRepository.playersPositionsByYearGroupTop(playType, yearGroup, topSize)
        );
    }

    List<PlayerTopPosition> playerTopContext(UUID playerId, Source source, PlayType playType, TopType topType) {
        return sortedPositions(
            topPlayersRepository.topPositionContext(topType, playerId, source, playType, topContextSize)
        );
    }

    Map<LocalDate, Integer> playerTopPositionHistory(UUID playerId, Source source, PlayType playType, TopType topType) {
        return topPlayersRepository.topPositionHistory(topType, playerId, source, playType).stream()
            .collect(Collectors.toMap(TopPositionHistoryRow::positionDate, TopPositionHistoryRow::position));
    }

    private List<PlayerTopPosition> sortedPositions(List<PlayerTopPosition> positions) {
        return positions.stream()
            .sorted(
                Comparator.comparingInt((PlayerTopPosition p) -> p.getRatingSnapshot().getPosition())
                    .thenComparing(pp -> pp.getPlayer().getDetails().getName())
                    .thenComparing(pp -> pp.getPlayer().getId())
            ).toList();

    }

    List<PlayerTopPosition> playerJuniorTopContext(UUID playerId, PlayType playType) {
        PlayerSearchResult player = playerRepository.getById(playerId)
            .orElseThrow(() -> new NoSuchElementException("player #%s not foind".formatted(playerId)));

        YearGroup yearGroup = YearGroup.ofBirthYear(player.getDetails().getYear());
        if (yearGroup == ALL) {
            return List.of();
        }

        return topPlayersRepository.playersPositionsByYearGroupContext(playerId, playType, yearGroup, topContextSize);
    }
}
