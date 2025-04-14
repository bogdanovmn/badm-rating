package com.github.bogdanovmn.badmrating.core.common;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SimilarStringsTest {
    @ParameterizedTest
    @MethodSource("provideFindClosestTestData")
    void testFindClosest(String value, Collection<String> strings, int editDistance, String expectedClosest) {
        SimilarStrings similarStrings = new SimilarStrings(strings, editDistance);
        String result = similarStrings.findClosest(value);
        assertEquals(expectedClosest, result,
            String.format("Ближайшая строка к '%s' из %s должна быть '%s', получено '%s'",
                value, strings, expectedClosest, result));
    }

    private static Stream<Arguments> provideFindClosestTestData() {
        return Stream.of(
            // Стандартный случай
            Arguments.of("Иванов", List.of("Иванов", "Иваонов", "Петров"), 1, "Иванов"),
            // Опечатка в имени
            Arguments.of("Иваонов", List.of("Иванов", "Петров"), 1, "Иванов"),
            // Пустая коллекция
            Arguments.of("Иванов", List.of(), 1, "Иванов"),
            // Одна строка
            Arguments.of("Иванов", List.of("Петров"), 1, "Иванов"),
            // Несколько строк с одинаковым расстоянием (берется первая по сравнению)
            Arguments.of("Иванов", List.of("Иваонов", "Ианов"), 1, "Иваонов"),
            // Пустая строка
            Arguments.of("", List.of("Иванов", "Петров"), 1, ""),
            // editDistance = 2 для большей гибкости
            Arguments.of("Ивонов", List.of("Иванов", "Петров"), 2, "Иванов")
        );
    }

    // Тесты для метода groups()
    @ParameterizedTest
    @MethodSource("provideGroupsTestData")
    void testGroups(Collection<String> input, List<Set<String>> expected) {
        SimilarStrings similarStrings = new SimilarStrings(input, 1);
        List<Set<String>> result = similarStrings.groups();
        assertEquals(
            expected.size(),
            result.size(),
            String.format("Ожидаемое количество групп: %d, получено: %d", expected.size(), result.size())
        );
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideGroupsTestData() {
        return Stream.of(
            // Кейс 1: Строки с опечатками (editDistance = 1)
            Arguments.of(
                List.of("Иванов", "Иваонов", "Ианов", "Петров"),
                List.of(
                    new HashSet<>(Set.of("Иванов", "Иваонов", "Ианов"))
                )
            ),
            // Кейс 2: Без опечаток
            Arguments.of(
                List.of("Иванов", "Петров", "Сидоров"),
                List.of()
            ),
            // Кейс 3: Пустой список
            Arguments.of(
                List.of(),
                List.of()
            ),
            // Кейс 4: Одна строка
            Arguments.of(
                List.of("Иванов"),
                List.of()
            ),
            // Кейс 5: Множественные группы
            Arguments.of(
                List.of("Иванов", "Иваонов", "Петров", "Петроов", "Сидоров"),
                List.of(
                    new HashSet<>(Set.of("Иванов", "Иваонов")),
                    new HashSet<>(Set.of("Петров", "Петроов"))
                )
            )
        );
    }
}