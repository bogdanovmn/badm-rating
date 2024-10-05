package com.github.bogdanovmn.badmrating.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlayerRank {
    MS("мс"), KMS("кмс"), MSMK("мсмк"), ZMS("змс"),
    R1("1"), R2("2"), R3("3"),
    BR("бр"),
    NO_RANK("");

    private final String name;

    public static PlayerRank of(String name) {
        for (PlayerRank rank : values()) {
            if (rank.name.equals(name.toLowerCase())) {
                return rank;
            }
        }
        throw new IllegalArgumentException("Unknown player rank: %s".formatted(name));
    }
}
