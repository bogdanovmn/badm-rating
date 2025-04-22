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
        return convertToFullName(
            normalizeFirstLetter(
                input.trim()
                    .replaceAll("\\s+", " ")
                    .replaceAll("[*.?]", "")
                    .replaceAll(SPLIT_NAME_PATTERN.pattern(), " ")
                    .replaceAll("ё", "е")
                    .replaceAll("Ё", "Е")
                    .replaceAll("([ЙУЕЫАОЭЯИЮЪйуеъыаояию])ь+", "$1")
                    .replaceFirst("Атем$", "Артем")
                    .replaceAll("([Кк])ирил([^л]|$)", "$1ирилл$2")
                    .replaceAll("\\d+$", "")
                    .trim()
            )
        );
    }

    private static String normalizeFirstLetter(String name) {
        Matcher matcher = REPLACE_FIRST_NAME_PATTERN.matcher(name);
        return matcher.find()
            ? matcher.group(1) + matcher.group(2).toUpperCase() + matcher.group(3)
            : name;
    }

    private static String convertToFullName(String name) {
        return name
            // Мужские имена
            .replaceFirst("Саша$", "Александр")
            .replaceFirst("Леша$", "Алексей")
            .replaceFirst("Тема$", "Артем")
            .replaceFirst("Боря$", "Борис")
            .replaceFirst("Вадик$", "Вадим")
//            .replaceFirst("Валя$", "Валентин")
            .replaceFirst("Вася$", "Василий")
            .replaceFirst("Витя$", "Виктор")
            .replaceFirst("Виталик$", "Виталий")
            .replaceFirst("Вова$", "Владимир")
            .replaceFirst("Володя$", "Владимир")
            .replaceFirst("Слава$", "Вячеслав")
            .replaceFirst("Жора$", "Георгий")
            .replaceFirst("Гриша$", "Григорий")
//            .replaceFirst("Даня$", "Даниил")
            .replaceFirst("Данил$", "Даниил")
            .replaceFirst("Дэн$", "Денис")
            .replaceFirst("Дима$", "Дмитрий")
//            .replaceFirst("Женя$", "Евгений")
            .replaceFirst("Ваня$", "Иван")
            .replaceFirst("Костя$", "Константин")
            .replaceFirst("Лева$", "Лев")
            .replaceFirst("Леня$", "Леонид")
            .replaceFirst("Макс$", "Максим")
            .replaceFirst("Миша$", "Михаил")
            .replaceFirst("Коля$", "Николай")
            .replaceFirst("Паша$", "Павел")
            .replaceFirst("Петя$", "Петр")
            .replaceFirst("Рома$", "Роман")
            .replaceFirst("Сережа$", "Сергей")
            .replaceFirst("Стас$", "Станислав")
            .replaceFirst("Степа$", "Степан")
            .replaceFirst("Федя$", "Федор")
            .replaceFirst("Юра$", "Юрий")
            .replaceFirst("Ярик$", "Ярослав")
            // Женские имена
            .replaceFirst("Настя$", "Анастасия")
            .replaceFirst("Аня$", "Анна")
            .replaceFirst("Тоня$", "Антонина")
//            .replaceFirst("Валя$", "Валентина")
            .replaceFirst("Варя$", "Варвара")
            .replaceFirst("Ника$", "Вероника")
            .replaceFirst("Вика$", "Виктория")
            .replaceFirst("Галя$", "Галина")
            .replaceFirst("Даша$", "Дарья")
            .replaceFirst("Катя$", "Екатерина")
            .replaceFirst("Катерина$", "Екатерина")
            .replaceFirst("Лена$", "Елена")
//            .replaceFirst("Алена$", "Елена")
            .replaceFirst("Лиза$", "Елизавета")
            .replaceFirst("Ира$", "Ирина")
            .replaceFirst("Клава$", "Клавдия")
            .replaceFirst("Ксюша$", "Ксения")
            .replaceFirst("Лара$", "Лариса")
            .replaceFirst("Лида$", "Лидия")
            .replaceFirst("Лиля$", "Лилия")
            .replaceFirst("Люба$", "Любовь")
            .replaceFirst("Люда$", "Людмила")
            .replaceFirst("Рита$", "Маргарита")
            .replaceFirst("Маша$", "Мария")
            .replaceFirst("Надя$", "Надежда")
            .replaceFirst("Наташа$", "Наталья")
            .replaceFirst("Наталия$", "Наталья")
            .replaceFirst("Леся$", "Олеся")
            .replaceFirst("Оля$", "Ольга")
            .replaceFirst("Поля$", "Полина")
            .replaceFirst("Рая$", "Раиса")
            .replaceFirst("Света$", "Светлана")
            .replaceFirst("Соня$", "София")
            .replaceFirst("Софья$", "София")
            .replaceFirst("Тома$", "Тамара")
            .replaceFirst("Таня$", "Татьяна")
            .replaceFirst("Уля$", "Ульяна")
            .replaceFirst("Юля$", "Юлия");
    }
}
