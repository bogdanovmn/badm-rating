package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
class NewPlayerRequest {
    @NotNull
    private final UUID playerId;
}
