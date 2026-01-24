package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@RequiredArgsConstructor
class PairRequest {
    @Size(min = 2, max = 2)
    List<UUID> pair;
}
