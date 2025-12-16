package com.github.bogdanovmn.badmrating.web.infrastructure.config.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("currentUserIdResolver")
public class CurrentUserIdResolver {
    public UUID resolve(Object principal) {
        if (principal instanceof JwtBasedUserDetails user) {
            return user.getUserId();
        }
        return null;
    }
}
