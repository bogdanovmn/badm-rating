package com.github.bogdanovmn.badmrating.web.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/player")
@RequiredArgsConstructor
class PlayerController {
    private final PlayerRatingService playerRatingService;

    @GetMapping("{id}")
    PlayerRating playerRating(@PathVariable("id") UUID playerId) {
        return playerRatingService.playerRatingHistory(playerId);
    }
}
