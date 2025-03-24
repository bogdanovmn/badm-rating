package com.github.bogdanovmn.badmrating.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {
    @ParameterizedTest
    @MethodSource("data")
    void testPlayerEquality(String name1, Integer year1, String region1,
                            String name2, Integer year2, String region2,
                            boolean expected) {
        Player player1 = Player.builder()
            .name(name1)
            .year(year1)
            .region(region1)
            .build();

        Player player2 = name2 == null ? null : Player.builder()
            .name(name2)
            .year(year2)
            .region(region2)
            .build();

        assertEquals(expected, player1.equals(player2));
    }

    private static Stream<Arguments> data() {
        return Stream.of(
            // name1, year1, region1, name2, year2, region2, expected
            Arguments.of("John", 1990, "EU", "John", 1990, "NA", true),     // Years match, regions different
            Arguments.of("John", 1990, "EU", "John", 1990, null, true),     // Years match, one region null
            Arguments.of("John", 1990, "EU", "John", 1985, "EU", false),    // Years different, regions match
            Arguments.of("John", null, "EU", "John", null, "EU", true),     // Years null, regions match
            Arguments.of("John", null, "EU", "John", null, "NA", false),    // Years null, regions different
            Arguments.of("John", null, null, "John", null, null, true),     // Years null, regions null
            Arguments.of("John", null, "EU", "John", null, null, false),    // Years null, one region null
            Arguments.of("John", 1990, "EU", "John", null, "EU", false),    // One year set, other null
            Arguments.of("John", 1990, "EU", "Mike", 1990, "EU", false),    // Names different
            Arguments.of("John", 1990, "EU", null, null, null, false)       // Other is null
        );
    }
}