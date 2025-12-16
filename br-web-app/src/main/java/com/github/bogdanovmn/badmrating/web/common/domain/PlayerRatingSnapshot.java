package com.github.bogdanovmn.badmrating.web.common.domain;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class PlayerRatingSnapshot {
    Integer position;
    Integer rating;
    Integer ratingChange;
    Integer positionChange;
    LocalDate updatedAt;
}
