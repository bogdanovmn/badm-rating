package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColumnValuePatternTest {

    @ParameterizedTest
    @MethodSource("provideColumnTestData")
    void testValuePattern(Column column, String input, boolean expectedMatch) {
        Pattern pattern = column.getValuePattern();
        assertEquals(expectedMatch, pattern.matcher(input).matches(),
            String.format("Строка '%s' должна %sсоответствовать шаблону %s",
                input, expectedMatch ? "" : "не ", column.name()));
    }

    private static Stream<Arguments> provideColumnTestData() {
        return Stream.of(
            // NAME
            Arguments.of(Column.NAME, "Шибанова Мария", true),
            Arguments.of(Column.NAME, "ШибановаМария", true),
            Arguments.of(Column.NAME, "Иванов И.О.", true),
            Arguments.of(Column.NAME, "Третьякова А.", true),
            Arguments.of(Column.NAME, "Иванов-Петров Иван", true),
            Arguments.of(Column.NAME, "Черепов Илья Игоревич", true),
            Arguments.of(Column.NAME, "Черепов Илья Игоревич 2", true),
            Arguments.of(Column.NAME, "Черепов* ИльяИгоревич", true),
            Arguments.of(Column.NAME, "Черепов* ИльяИгоревич ???", true),
            Arguments.of(Column.NAME, "Кадыров (Глинкин) Тимур", true),
            Arguments.of(Column.NAME, "Кадыров* (Глинкин) Тимур ?", true),
            Arguments.of(Column.NAME, "123 Иван", false),
            Arguments.of(Column.NAME, "", false),

            // BIRTHDAY
            Arguments.of(Column.BIRTHDAY, "1990", true),
            Arguments.of(Column.BIRTHDAY, "2023.0", true),
            Arguments.of(Column.BIRTHDAY, "   1985   ", true),
            Arguments.of(Column.BIRTHDAY, "85", false),
            Arguments.of(Column.BIRTHDAY, "двадцать", false),
            Arguments.of(Column.BIRTHDAY, "", false),

            // RANK
            Arguments.of(Column.RANK, "мс", true),
            Arguments.of(Column.RANK, "кмс", true),
            Arguments.of(Column.RANK, "кис", true),
            Arguments.of(Column.RANK, "мсмк", true),
            Arguments.of(Column.RANK, "змс", true),
            Arguments.of(Column.RANK, "1", true),
            Arguments.of(Column.RANK, "1 р", true),
            Arguments.of(Column.RANK, "iр", true),
            Arguments.of(Column.RANK, "i", true),
            Arguments.of(Column.RANK, "2", true),
            Arguments.of(Column.RANK, "iiр", true),
            Arguments.of(Column.RANK, "3", true),
            Arguments.of(Column.RANK, "iю", true),
            Arguments.of(Column.RANK, "1ю", true),
            Arguments.of(Column.RANK, "1 ю", true),
            Arguments.of(Column.RANK, "1 юн.", true),
            Arguments.of(Column.RANK, "iiю", true),
            Arguments.of(Column.RANK, "2 ю", true),
            Arguments.of(Column.RANK, "3ю", true),
            Arguments.of(Column.RANK, "iiiю", true),
            Arguments.of(Column.RANK, "бр", true),
            Arguments.of(Column.RANK, "б/р", true),
            Arguments.of(Column.RANK, "бз", true),
            Arguments.of(Column.RANK, "б/з", true),
            Arguments.of(Column.RANK, "мс.0", true),
            Arguments.of(Column.RANK, "   кмс   ", true),
            Arguments.of(Column.RANK, "мастер", false),
            Arguments.of(Column.RANK, "123", false),
            Arguments.of(Column.RANK, "", false),

            // REGION
            Arguments.of(Column.REGION, "МОС", true),
            Arguments.of(Column.REGION, "МОС/СПБ", true),
            Arguments.of(Column.REGION, "   НСК   ", true),
            Arguments.of(Column.REGION, "МОС\\ЕКБ", true),
            Arguments.of(Column.REGION, "МО", false),
            Arguments.of(Column.REGION, "123", false),
            Arguments.of(Column.REGION, "", false),

            // SCORE
            Arguments.of(Column.SCORE, "123", true),
            Arguments.of(Column.SCORE, "45.67", true),
            Arguments.of(Column.SCORE, "   100.5   ", true),
            Arguments.of(Column.SCORE, "abc", false),
            Arguments.of(Column.SCORE, "12.34.56", false),
            Arguments.of(Column.SCORE, "", false)
        );
    }
}