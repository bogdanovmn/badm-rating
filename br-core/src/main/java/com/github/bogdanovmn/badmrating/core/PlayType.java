package com.github.bogdanovmn.badmrating.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public enum PlayType {
    MS("МО", "MS"), MD("МП", "MD"),
    WS("ЖО", "WS"), WD("ЖП", "WD"),
    XD("МС", "ЖС", "XD(M)", "XD(W)", "XD_M", "XD_W"),
    UNKNOWN;

    private final Set<String> possibleTitles;
    private final Set<Pattern> patterns;

    PlayType(String... possibleTitles) {
        this.possibleTitles = new HashSet<>(List.of(possibleTitles));
        this.patterns = new HashSet<>();
        for (String title : possibleTitles) {
            String regex = "(^|\\P{L})" + Pattern.quote(title) + "(\\P{L}|$)";
            patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
    }

    public static PlayType of(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return UNKNOWN;
        }
        String name = rawName.toUpperCase().trim();
        for (PlayType type : values()) {
            if (type.possibleTitles.contains(name)) {
                return type;
            }
        }
        for (PlayType type : values()) {
            for (Pattern pattern : type.patterns) {
                if (pattern.matcher(name).find() && !name.contains("(ОШИБ)")) {
                    return type;
                }
            }
        }
        return UNKNOWN;
    }
}
