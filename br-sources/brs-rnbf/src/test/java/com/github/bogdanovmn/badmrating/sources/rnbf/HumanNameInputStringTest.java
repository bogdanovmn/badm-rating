package com.github.bogdanovmn.badmrating.sources.rnbf;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HumanNameInputStringTest {

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of("ИвановИван", "Иванов Иван"),
            Arguments.of("  ИвановИван ", "Иванов Иван"),
            Arguments.of("ИвановИванИванович", "Иванов Иван Иванович"),
            Arguments.of("ИвановИванИванович Третий", "Иванов Иван Иванович Третий"),
            Arguments.of("Иванов* ИванИванович", "Иванов Иван Иванович"),
            Arguments.of("Иванов* ИванИванович ???", "Иванов Иван Иванович"),
            Arguments.of("Иванов ИванИванович.", "Иванов Иван Иванович")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void normalized(String input, String normalized) {
        assertEquals(
            normalized,
            new HumanNameInputString(input).normalized()
        );
    }
}