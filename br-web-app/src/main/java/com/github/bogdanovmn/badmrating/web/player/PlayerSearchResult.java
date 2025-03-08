package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.Player;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
class PlayerSearchResult {
    UUID id;
    Player details;
}
