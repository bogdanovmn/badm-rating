package com.github.bogdanovmn.badmrating.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public enum PlayerRank {
    MS("мс"), KMS("кмс", "кис"), MSMK("мсмк"), ZMS("змс"),
    R1("1", "1 р", "iр", "i"), R2("2", "iiр"), R3("3"),
    U1("iю", "1ю", "1 юн.", "1 юн"), U2("iiю", "2ю", "2 юн.", "2 юн"), U3("3ю", "iiiю", "3 юн.", "3 юн"),
    BR("бр", "б/р", "б\\р"),
    BZ("бз", "б/з", "б\\з"),
    NO_RANK("");

    private final Set<String> possibleTitles;

    PlayerRank(String... possibleTitles) {
        this.possibleTitles = new HashSet<>(List.of(possibleTitles));
    }

    public static PlayerRank of(String name) {
        for (PlayerRank rank : values()) {
            if (rank.possibleTitles.contains(name.toLowerCase())) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown player rank: '%s'".formatted(name));
    }
}
