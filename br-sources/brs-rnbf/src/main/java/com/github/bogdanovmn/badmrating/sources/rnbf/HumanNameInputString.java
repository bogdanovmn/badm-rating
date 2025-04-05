package com.github.bogdanovmn.badmrating.sources.rnbf;

import lombok.RequiredArgsConstructor;

import java.util.regex.Pattern;

@RequiredArgsConstructor
public class HumanNameInputString {
    /**
     (?<=\\p{Ll}): Положительный просмотр назад, который убеждается,
            что предыдущий символ является строчной буквой (\\p{Ll}).
     (?=\\p{Lu}): Положительный просмотр вперед, который убеждается,
            что следующий символ является заглавной буквой (\\p{Lu}).

     Это регулярное выражение находит все позиции в строке, где заглавная буква следует за строчной буквой,
     и вставляет пробел перед каждой заглавной буквой. При этом оно не добавляет лишние пробелы
     перед заглавными буквами, которые уже разделены пробелами.
     */
    private static final Pattern SPLIT_NAME_PATTERN = Pattern.compile("(?<=\\p{Ll})(?=\\p{Lu})");

    private final String input;

    public String normalized() {
        return input.trim()
            .replaceAll(SPLIT_NAME_PATTERN.pattern(), " ")
            .replaceAll("\\d+$", "")
            .replaceAll("\\s+", " ")
            .replaceAll("[*.?]", "")
            .trim();
    }
}
