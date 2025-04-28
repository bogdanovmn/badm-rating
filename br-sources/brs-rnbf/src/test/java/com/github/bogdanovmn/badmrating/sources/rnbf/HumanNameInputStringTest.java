package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PlayType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.github.bogdanovmn.badmrating.core.PlayType.Sex.FEMALE;
import static com.github.bogdanovmn.badmrating.core.PlayType.Sex.MALE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HumanNameInputStringTest {

    private static Stream<Arguments> data() {
        return Stream.of(
            Arguments.of(MALE, "ИвановИван", "Иванов Иван"),
            Arguments.of(MALE, "  ИвановИван ", "Иванов Иван"),
            Arguments.of(MALE, "ИвановИванИванович", "Иванов Иван Иванович"),
            Arguments.of(MALE, "ИвановИванИванович Третий", "Иванов Иван Иванович Третий"),
            Arguments.of(MALE, "Иванов* ИванИванович", "Иванов Иван Иванович"),
            Arguments.of(MALE, "Иванов* ИванИванович ???", "Иванов Иван Иванович"),
            Arguments.of(MALE, "Иванов ИванИванович.", "Иванов Иван Иванович"),
            Arguments.of(MALE, "Иванов ИванИванович 2", "Иванов Иван Иванович"),
            Arguments.of(MALE, "Иванов Атем", "Иванов Артем"),
            Arguments.of(FEMALE, "Иванова Софья", "Иванова София"),
            Arguments.of(FEMALE, "Иванова Наталия", "Иванова Наталья"),
            Arguments.of(FEMALE, "Иванова Лена", "Иванова Елена"),
            Arguments.of(FEMALE, "Юляшкина Юля", "Юляшкина Юлия"),
            Arguments.of(FEMALE, "Ёжиков Рома", "Ежиков Роман"),
            Arguments.of(MALE, "Кирилов Кирил", "Кириллов Кирилл"),
            Arguments.of(MALE, "Кирилов кирил", "Кириллов Кирилл"),
            Arguments.of(FEMALE, "Кирилова Валя", "Кириллова Валентина"),
            Arguments.of(MALE, "Паььнкратов Дмиьтрий", "Панкратов Дмитрий")
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void normalized(PlayType.Sex sex, String input, String normalized) {
        assertEquals(
            normalized,
            new HumanNameInputString(input).normalized(sex)
        );
    }
}