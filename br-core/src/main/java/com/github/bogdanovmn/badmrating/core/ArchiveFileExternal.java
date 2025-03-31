package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@Builder
@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ArchiveFileExternal {
    @NonNull
    @EqualsAndHashCode.Include
    LocalDate date;

    @NonNull
    String url;

    @NonNull
    String urlText;

    record DatePattern(Pattern pattern, boolean withDayCorrection) {}
    record TitleDatePattern(Pattern pattern, String dateFormat, boolean withYear) {}

    private static final List<DatePattern> DATES = List.of(
        new DatePattern(
            Pattern.compile("^.*/(?<year>\\d{4})/rank(ing)?_?(?<day1>\\d\\d)_(?<month>\\d\\d)_(?<day2>\\d\\d)(\\D.*)?\\.xls$"),
            true
        ),
        new DatePattern(
            Pattern.compile("^.*rank(\\s+|_)?(?<year>20\\d\\d)_?(?<month>\\d\\d)_?(?<day>\\d\\d)(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*/(?<year>\\d{4})/(rank|ranking|gp|ss)(\\s+|_)?(?<day>\\d\\d)([_.])?(?<month>\\d\\d)([_.])?\\k<year>(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*/(?<year>\\d{4})/(rank|ranking|gp|ss)(\\s+|_)?(?<day>\\d\\d)([_.])(?<month>\\d\\d)([_.])\\d\\d(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*ranking/(?<year>\\d{4})/rank(ing|_gp)?_(?<day>\\d\\d)_(?<month>\\d\\d)(\\(\\d+\\))?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*/20\\d{2}/rank_(?<day>\\d\\d)_(?<month>\\d\\d)_(?<year>\\d{4})\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*ranking/(?<year>20\\d\\d)_(?<month>\\d\\d?)_(?<day>\\d\\d?)\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*(ranking|spisok)(?<day>\\d\\d)(?<month>\\d\\d)(?<year>20\\d\\d)(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        )
    );

    private static final List<TitleDatePattern> TITLE_DATES = List.of(
        new TitleDatePattern(
            Pattern.compile("^.*\\D?(?<date>([1-9]|[12]\\d|3[01])\\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)\\s+\\d{4}).*$", Pattern.CASE_INSENSITIVE),
            "d MMMM yyyy",
            true
        ),
        new TitleDatePattern(
            Pattern.compile("^.*?\\D?(?<date>\\d{1,2}\\.\\d{1,2}\\.\\d{4}).*$"),
            "dd.MM.yyyy",
            true
        ),
        new TitleDatePattern(
            Pattern.compile("^.*(?<date>([1-9]|[12]\\d|3[01])\\s+(января|февраля|марта|апреля|мая|июня|июля|августа|сентября|октября|ноября|декабря)).*$", Pattern.CASE_INSENSITIVE),
            "d MMMM yyyy",
            false
        )
    );

    private static final Pattern URL_YEAR_PATTERN = Pattern.compile("^.*/(20\\d\\d)/.*$", Pattern.CASE_INSENSITIVE);

    public static ArchiveFileExternal of(String url, String text) {
        for (DatePattern datePattern : DATES) {
            Matcher matcher = datePattern.pattern.matcher(url);
            if (matcher.find()) {
                return ArchiveFileExternal.builder()
                    .url(url)
                    .urlText(text)
                    .date(
                        LocalDate.of(
                            Integer.parseInt(matcher.group("year")),
                            Integer.parseInt(matcher.group("month")),
                            datePattern.withDayCorrection
                                ? matcher.group("year").substring(2, 4).equals(matcher.group("day1"))
                                    ? Integer.parseInt(matcher.group("day2"))
                                    : Integer.parseInt(matcher.group("day1"))
                                : Integer.parseInt(matcher.group("day"))
                        )
                    )
                .build();
            }
        }
        log.debug("Can't parse date by url: {}", url);
        log.debug("Trying to parse date by title: '{}'", text);
        String year = null;
        Matcher yearMatcher = URL_YEAR_PATTERN.matcher(url);
        if (yearMatcher.find()) {
            year = yearMatcher.group(1);
        }
        for (TitleDatePattern titleDatePattern : TITLE_DATES) {
            Matcher matcher = titleDatePattern.pattern.matcher(text);
            if (matcher.find()) {
                return ArchiveFileExternal.builder()
                    .url(url)
                    .urlText(text)
                    .date(
                        LocalDate.parse(
                            matcher.group("date") + (titleDatePattern.withYear ? "" : " " + year),
                            DateTimeFormatter.ofPattern(titleDatePattern.dateFormat)
                                .withLocale(Locale.forLanguageTag("ru"))
                        )
                    )
                .build();
            }
        }
        log.warn("Can't parse date by title: '{}'", text);
        return null;
    }
}
