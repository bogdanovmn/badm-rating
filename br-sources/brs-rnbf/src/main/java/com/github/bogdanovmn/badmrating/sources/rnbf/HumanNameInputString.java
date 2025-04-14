package com.github.bogdanovmn.badmrating.sources.rnbf;

import lombok.RequiredArgsConstructor;

import java.util.regex.Matcher;
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

    private static final Pattern REPLACE_FIRST_NAME_PATTERN = Pattern.compile("^(\\p{Lu}\\p{L}+\\s+)(\\p{Ll})(\\p{L}*)$");

    private final String input;

    public String normalized() {
        return normalizeFirstLetter(
            input.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[*.?]", "")
                .replaceAll(SPLIT_NAME_PATTERN.pattern(), " ")
                .replaceAll("ё", "е")
                .replaceAll("Ё", "Е")
                .replaceAll("([ЙУЕЫАОЭЯИЮЪйуеъыаояию])ь+", "$1")
                .replaceFirst("Алена$", "Елена")
                .replaceFirst("Атем$", "Артем")
                .replaceFirst("Данил$", "Даниил")
                .replaceFirst("София$", "Софья")
                .replaceFirst("Наталия$", "Наталья")
                .replaceFirst("Юля$", "Юлия")
                .replaceAll("([Кк])ирил([^л]|$)", "$1ирилл$2")
                .replaceAll("\\d+$", "")
                .trim()
        );
    }

    private String normalizeFirstLetter(String name) {
        Matcher matcher = REPLACE_FIRST_NAME_PATTERN.matcher(name);
        return matcher.find()
            ? matcher.group(1) + matcher.group(2).toUpperCase() + matcher.group(3)
            : name;
    }
}
