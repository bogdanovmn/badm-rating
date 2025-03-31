package com.github.bogdanovmn.badmrating.cli;

import com.github.bogdanovmn.badmrating.core.RatingSource;
import com.github.bogdanovmn.badmrating.sources.rnbf.RussianNationalBadmintonFederation;
import com.github.bogdanovmn.badmrating.sources.rnbf.junior.RussianNationalBadmintonFederationJunior;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
enum SourceType {
    RNBF(RussianNationalBadmintonFederation.class),
    RNBFJunior(RussianNationalBadmintonFederationJunior.class);

    private final Class<? extends RatingSource> ratingSourceClass;

    public RatingSource ratingSourceInstance() {
        try {
            return ratingSourceClass.getConstructor().newInstance();
        } catch (Exception ex) {
            throw new IllegalStateException(
                String.format("Can't create instance of '%s'", ratingSourceClass),
                ex
            );
        }
    }
}