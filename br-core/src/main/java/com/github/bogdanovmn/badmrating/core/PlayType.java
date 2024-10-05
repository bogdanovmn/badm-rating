package com.github.bogdanovmn.badmrating.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum PlayType {
    MS("МО"), MD("МП"),
    WS("ЖО"), WD("ЖП"),
    XD("МС", "ЖС"),
    UNKNOWN;

    private final Set<String> possibleTitles;

    PlayType(String... possibleTitles) {
        this.possibleTitles = new HashSet<>(List.of(possibleTitles));
    }

    public static PlayType of(String rawName) {
        String name = rawName.toUpperCase();
        for (PlayType type : values()) {
            if (type.possibleTitles.contains(name)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
