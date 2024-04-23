package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Set;
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

    private static final Set<DatePattern> DATES = Set.of(
        new DatePattern(
            Pattern.compile("^.*/(?<year>\\d{4})/rank_?(?<day1>\\d\\d)_(?<month>\\d\\d)_(?<day2>\\d\\d)(\\D.*)?\\.xls$"),
            true
        ),
        new DatePattern(
            Pattern.compile("^.*rank(\\s+|_)?(?<year>20\\d\\d)_?(?<month>\\d\\d)_?(?<day>\\d\\d)(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
            false
        ),
        new DatePattern(
            Pattern.compile("^.*(rank|ranking|gp|ss)(\\s+|_)?(?<day>\\d\\d)_(?<month>\\d\\d)_(?<year>\\d{4})(\\D.*)?\\.xls$", Pattern.CASE_INSENSITIVE),
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
