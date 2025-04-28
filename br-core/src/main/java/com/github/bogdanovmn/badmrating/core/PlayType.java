package com.github.bogdanovmn.badmrating.core;

import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.github.bogdanovmn.badmrating.core.PlayType.Sex.FEMALE;
import static com.github.bogdanovmn.badmrating.core.PlayType.Sex.MALE;
import static com.github.bogdanovmn.badmrating.core.PlayType.Sex.MIXED;

public enum PlayType {
    MS(MALE, "МО", "MS"), MD(MALE, "МП", "MD"),
    WS(FEMALE, "ЖО", "WS"), WD(FEMALE,"ЖП", "WD"),
    XD(MIXED, "!NOT FOR PARSING!"),
    MXD(MALE, "МС", "XD(M)", "XD_M", "СПМ"),
    WXD(FEMALE, "ЖС", "XD(W)", "XD_W", "СПЖ");

    @Getter
    private final Sex sex;
    private final Set<String> possibleTitles;
    private final Set<Pattern> patterns;

    public enum Sex { MALE, FEMALE, MIXED }

    PlayType(Sex sex, String... possibleTitles) {
        this.sex = sex;
        this.possibleTitles = new HashSet<>(List.of(possibleTitles));
        this.patterns = new HashSet<>();
        for (String title : possibleTitles) {
            String regex = "(^|\\P{L})" + Pattern.quote(title) + "(\\P{L}|$)";
            patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
        }
    }

    public static PlayType of(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            return null;
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
        return null;
    }
}
