package com.github.bogdanovmn.badmrating.sources.rnbf;

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

    private static final Set<Pattern> DATES = Set.of(
        Pattern.compile("^.*rank_(20\\d\\d)(\\d\\d)(\\d\\d)\\.xls$"),
        Pattern.compile("^.*rank_(20\\d\\d)(\\d\\d)(\\d\\d)\\D.*\\.xls$")
    );

    public static ArchiveFileExternal of(String url) {
        for (Pattern datePattern : DATES) {
            Matcher matcher = datePattern.matcher(url);
            if (matcher.find()) {
                return ArchiveFileExternal.builder()
                    .url(url)
                    .date(
                        LocalDate.of(
                            Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3))
                        )
                    )
                    .build();
            }
        }
        log.warn("Can't parse date: %s".formatted(url));
        return null;
    }
}
