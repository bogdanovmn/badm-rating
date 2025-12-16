package com.github.bogdanovmn.badmrating.web.top;

import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRatingSnapshot;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerSearchResult;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PlayerTopPosition {
    PlayerSearchResult player;
    PlayerRatingSnapshot ratingSnapshot;
}
