package com.github.bogdanovmn.badmrating.web.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
class PlayerRatingService {
    private final PlayerRatingRepository playerRatingRepository;

    PlayerRating playerRatingHistory(UUID playerId) {
        return playerRatingRepository.playerRatingHistory(playerId);
    }

}
