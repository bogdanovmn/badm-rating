package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class PlayerService {
    private final PlayerRepository playerRepository;
    private final LevenshteinDistance levenshteinDistance = new LevenshteinDistance(1);

    List<PlayerSearchResult> search(String term) {
        return playerRepository.findByTerm(term);
    }

    List<PlayerSearchResult> similarities(UUID playerId) {
        PlayerSearchResult player = playerRepository.getById(playerId);
        if (player == null) {
            throw new NoSuchElementException(
                String.format("Player with id %s not found", playerId)
            );
        }
        List<PlayerSearchResult> candidates = playerRepository.findSimilaritiesCandidates(
            player.getDetails().getName()
        );
        return candidates.stream()
            .filter(c -> !c.getId().equals(playerId))
            .filter(c -> {
                int distance = levenshteinDistance.apply(c.getDetails().getName(), player.getDetails().getName());
                return distance >= 0 && distance <= 1;
            })
            .toList();
    }
}
