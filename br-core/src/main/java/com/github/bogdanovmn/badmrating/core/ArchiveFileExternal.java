package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@Builder
@Slf4j
public class ArchiveFileExternal {
    @NonNull
    LocalDate date;
    @NonNull
    String url;

    record DatePattern(Pattern pattern, boolean withDayCorrection) {}

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

    public static ArchiveFileExternal of(String url) {
        for (DatePattern datePattern : DATES) {
            Matcher matcher = datePattern.pattern.matcher(url);
            if (matcher.find()) {
                return ArchiveFileExternal.builder()
                    .url(url)
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
        log.warn("Can't parse date: %s".formatted(url));
        return null;
    }
}
