package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.PlayerRatingSnapshot;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
class PlayerRatingState {
    Source source;
    PlayType playType;
    PlayerRatingSnapshot ratingSnapshot;
}
