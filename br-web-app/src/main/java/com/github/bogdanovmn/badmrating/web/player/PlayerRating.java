package com.github.bogdanovmn.badmrating.web.player;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.web.common.domain.Source;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Map;

@Value
@Builder
class PlayerRating {
    Source source;
    PlayType playType;
    Map<LocalDate, Integer> data;
}
