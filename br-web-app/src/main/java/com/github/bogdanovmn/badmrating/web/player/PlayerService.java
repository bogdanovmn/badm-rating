package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRepository;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class PlayerService {
    private final PlayerRepository playerRepository;

    List<PlayerSearchResult> search(String term) {
        return playerRepository.findByTerm(term);
    }
}
