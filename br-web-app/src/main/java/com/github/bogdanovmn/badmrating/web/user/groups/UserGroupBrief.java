package com.github.bogdanovmn.badmrating.web.user.groups;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
class UserGroupBrief {
    UUID id;
    String name;
    int playersCount;
}
