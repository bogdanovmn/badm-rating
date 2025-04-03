package com.github.bogdanovmn.badmrating.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayTypeTest {

    @ParameterizedTest
    @MethodSource("testCasesProvider")
    void shouldCorrectlyIdentifyPlayType(String input, PlayType expected) {
//        assertEquals(PlayType.XD, PlayType.of("XD"));
        assertEquals(expected, PlayType.of(input));
    }

    private static Stream<Arguments> testCasesProvider() {
        return Stream.of(
            // Точные совпадения
            Arguments.of("МО", PlayType.MS),
            Arguments.of("MS", PlayType.MS),
            Arguments.of("ms", PlayType.MS),
            Arguments.of("МП", PlayType.MD),
            Arguments.of("MD", PlayType.MD),
            Arguments.of("md", PlayType.MD),
            Arguments.of("ЖО", PlayType.WS),
            Arguments.of("WS", PlayType.WS),
            Arguments.of("ws", PlayType.WS),
            Arguments.of("ЖП", PlayType.WD),
            Arguments.of("WD", PlayType.WD),
            Arguments.of("wd", PlayType.WD),
            Arguments.of("МС", PlayType.XD),
            Arguments.of("ЖС", PlayType.XD),
            Arguments.of("XD", PlayType.UNKNOWN),
            Arguments.of("xd", PlayType.UNKNOWN),
            Arguments.of("XD(M)", PlayType.XD),
            Arguments.of("XD(W)", PlayType.XD),
            Arguments.of("XD_M", PlayType.XD),
            Arguments.of("XD_W", PlayType.XD),

            // Частичные совпадения
            Arguments.of("Категория XD(W)", PlayType.XD),
            Arguments.of("SomeTextMD", PlayType.UNKNOWN),
            Arguments.of("WS-2023", PlayType.UNKNOWN),
            Arguments.of("97-моложе XD_M", PlayType.XD),

            // Неправильные/неизвестные значения
            Arguments.of("invalid", PlayType.UNKNOWN),
            Arguments.of("XYZ", PlayType.UNKNOWN),
            Arguments.of("", PlayType.UNKNOWN),
            Arguments.of("   ", PlayType.UNKNOWN),
            Arguments.of("XD(OШИБ)", PlayType.UNKNOWN),
            Arguments.of("MS_ERROR", PlayType.UNKNOWN),

            // Граничные случаи
            Arguments.of(null, PlayType.UNKNOWN),
            Arguments.of("МОЖО", PlayType.UNKNOWN), // Не должно совпадать с "МО" + "ЖО"
            Arguments.of("X D", PlayType.UNKNOWN)   // Пробел внутри
        );
    }
}