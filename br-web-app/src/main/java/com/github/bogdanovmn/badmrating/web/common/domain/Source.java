package com.github.bogdanovmn.badmrating.web.common.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Source {
    RNBF(1),
    RNBFJunior(2),
    RNBFVeteran(3);

    @Getter
    private final int id;

    public static Source byId(int sourceId) {
        for (Source source : values()) {
            if (source.getId() == sourceId) {
                return source;
            }
        }
        throw new IllegalArgumentException("No such source with id=" + sourceId);
    }
}
