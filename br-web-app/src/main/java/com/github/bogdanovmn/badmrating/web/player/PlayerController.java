package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
@Slf4j
class PlayerController {
    private final PlayerRatingService playerRatingService;
    private final PlayerService playerService;

    @GetMapping("{id}")
    List<PlayerRating> playerRating(@PathVariable("id") UUID playerId) {
        return playerRatingService.playerRatingHistory(playerId);
    }

    @GetMapping
    List<PlayerSearchResult> playerSearch(@RequestParam("term") String term) {
        log.debug("Search term '{}'", term);
        if (term.length() > 2) {
           return playerService.search(term);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @GetMapping("{id}/similarities")
    List<PlayerSearchResult> playerSimilarities(@PathVariable("id") UUID playerId) {
        return playerService.similarities(playerId);
    }
}
