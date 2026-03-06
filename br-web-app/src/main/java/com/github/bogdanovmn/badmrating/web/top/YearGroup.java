package com.github.bogdanovmn.badmrating.web.top;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;

@RequiredArgsConstructor
enum YearGroup {
    U19(19),
    U17(17),
    U15(15),
    U13(13),
    ALL(-1);

    @Getter
    private final int maxAge;

    record YearsInterval(int from, int to) {}

    public YearsInterval years() {
        int currentYear = LocalDate.now().getYear();
        return maxAge < 0
            ? null
            : new YearsInterval(
                currentYear - this.maxAge + 1,
                currentYear - this.maxAge + 2
            );
    }

    public static YearGroup ofBirthYear(int birthYear) {
        int currentYear = LocalDate.now().getYear();
        int ageThisYear = currentYear - birthYear;

        return Arrays.stream(values())
            .filter(group -> group.maxAge >= ageThisYear)
            .min(Comparator.comparingInt(YearGroup::getMaxAge))
            .orElse(ALL);
    }

}
