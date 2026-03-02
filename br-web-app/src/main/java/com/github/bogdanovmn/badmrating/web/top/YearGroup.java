package com.github.bogdanovmn.badmrating.web.top;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

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
}
