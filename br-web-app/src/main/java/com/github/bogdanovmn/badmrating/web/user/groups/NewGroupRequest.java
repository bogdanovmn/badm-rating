package com.github.bogdanovmn.badmrating.web.user.groups;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
class NewGroupRequest {
    @NotBlank
    private final String name;
}
