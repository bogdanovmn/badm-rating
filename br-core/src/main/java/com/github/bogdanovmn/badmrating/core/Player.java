package com.github.bogdanovmn.badmrating.core;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Player {
    String name;
    Integer year;
    String region;
    PlayerRank rank;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player other = (Player) o;

        if (this.name == null || !this.name.equals(other.name)) {
            return false;
        }

        boolean thisYearSet = this.year != null;
        boolean otherYearSet = other.year != null;

        if (thisYearSet && otherYearSet) {
            return this.year.equals(other.year);
        }

        if (!thisYearSet && !otherYearSet) {
            if (this.region == null && other.region == null) {
                return true;
            }
            if (this.region == null || other.region == null) {
                return false;
            }
            return this.region.equals(other.region);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        if (year != null) {
            result = 31 * result + year.hashCode();
        } else if (region != null) {
            result = 31 * result + region.hashCode();
        }
        return result;
    }
}
