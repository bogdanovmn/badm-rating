package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public
class Player {
    @EqualsAndHashCode.Include
    String name;
    @EqualsAndHashCode.Include
    int year;
    String region;
    PlayerRank rank;
}
