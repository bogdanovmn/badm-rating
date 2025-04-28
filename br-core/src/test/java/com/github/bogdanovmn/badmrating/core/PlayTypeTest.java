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
            Arguments.of("МС", PlayType.MXD),
            Arguments.of("ЖС", PlayType.WXD),
            Arguments.of("XD", null),
            Arguments.of("xd", null),
            Arguments.of("XD(M)", PlayType.MXD),
            Arguments.of("XD(W)", PlayType.WXD),
            Arguments.of("XD_M", PlayType.MXD),
            Arguments.of("XD_W", PlayType.WXD),

            // Частичные совпадения
            Arguments.of("XD_W 05-06", PlayType.WXD),
            Arguments.of(" MD 07 и моложе", PlayType.MD),
            Arguments.of("Категория XD(W)", PlayType.WXD),
            Arguments.of("SomeTextMD", null),
            Arguments.of("WS-2023", PlayType.WS),
            Arguments.of("97-моложе XD_M", PlayType.MXD),

            // Неправильные/неизвестные значения
            Arguments.of("invalid", null),
            Arguments.of("XYZ", null),
            Arguments.of("", null),
            Arguments.of("   ", null),
            Arguments.of("XD(OШИБ)", null),

            // Граничные случаи
            Arguments.of(null, null),
            Arguments.of("МОЖО", null), // Не должно совпадать с "МО" + "ЖО"
            Arguments.of("X D", null)   // Пробел внутри
        );
    }
}