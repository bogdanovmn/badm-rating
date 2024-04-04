package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class Player {
    String name;
    int year;
    String region;
    String rank;
    Integer bwfId; // id в системе мирового рейтинга
}
