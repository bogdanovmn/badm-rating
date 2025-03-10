package com.github.bogdanovmn.badmrating.web.common.domain;

import com.github.bogdanovmn.badmrating.core.Player;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class PlayerSearchResult {
    UUID id;
    Player details;
}
