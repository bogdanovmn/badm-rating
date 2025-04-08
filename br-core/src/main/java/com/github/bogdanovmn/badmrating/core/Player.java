package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class Player {
    @NonNull
    String name;
    Integer year;
    String region;
    PlayerRank rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player other = (Player) o;

        if (!this.name.equals(other.name)) {
            return false;
        }

        boolean thisYearSet = this.year != null;
        boolean otherYearSet = other.year != null;
        boolean yearsEqual = thisYearSet && otherYearSet && this.year.equals(other.year);
        boolean noYears = !thisYearSet && !otherYearSet;

        boolean thisRegionSet = this.region != null;
        boolean otherRegionSet = other.region != null;
        boolean regionsEqual = thisRegionSet && otherRegionSet && this.region.equals(other.region);

        boolean noYearNoRegion = !thisYearSet && !thisRegionSet && !otherYearSet && !otherRegionSet;

        return yearsEqual || noYears && regionsEqual || noYearNoRegion;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        if (year != null) {
            result = 31 * result + year.hashCode();
        }
        return result;
    }
}
