package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonalRating {
    Player player;
    PlayType type;
    int value;
}
