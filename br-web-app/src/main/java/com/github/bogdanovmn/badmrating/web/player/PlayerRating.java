package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
class PlayerRating {
    int value;
    PlayType playType;
    LocalDate date;
}
