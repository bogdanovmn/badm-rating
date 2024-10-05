package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public
class Player {
    String name;
    int year;
    String region;
    PlayerRank rank;
}
