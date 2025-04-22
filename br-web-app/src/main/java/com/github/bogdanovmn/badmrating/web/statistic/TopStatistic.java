package com.github.bogdanovmn.badmrating.web.statistic;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
@Builder
class TopStatistic {
    PlayType type;
    String source;
    List<PlayerPosition> data;

    @Value
    @Builder
    static class PlayerPosition {
        PlayerSearchResult player;
        Integer position;
        Integer rating;
        Integer ratingChange;
        Integer positionChange;
        LocalDate updatedAt;
    }
}
