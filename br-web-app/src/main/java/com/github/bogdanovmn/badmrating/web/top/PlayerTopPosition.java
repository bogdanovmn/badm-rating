package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
class PlayerTopPosition {
    PlayerSearchResult player;
    Integer position;
    Integer rating;
    Integer ratingChange;
    Integer positionChange;
    LocalDate updatedAt;
}
