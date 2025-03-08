package com.github.bogdanovmn.badmrating.web.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class PlayerSearchService {
    private final PlayerRepository playerRepository;

    List<PlayerSearchResult> search(String term) {
        return playerRepository.findByTerm(term);
    }
}
