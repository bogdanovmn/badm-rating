package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import com.github.bogdanovmn.badmrating.core.RatingType;
import com.github.bogdanovmn.badmrating.core.excel.ExcelCell;
import com.github.bogdanovmn.badmrating.core.excel.ExcelRow;
import com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.BIRTHDAY;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.NAME;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.RANK;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.SCORE;

@RequiredArgsConstructor
@Slf4j
class ResultTableRow {
    private final ResultTableHeader header;
    private final ExcelRow row;

    Optional<PersonalRating> fetch() {
        String name = Optional.ofNullable(
            row.cellStringValue(header.nameIndex())
        ).map(n -> new HumanNameInputString(n).normalized())
            .orElse(null);
        if (name == null) {
            log.warn("Can't find proper name. Skip record #{}", row.index());
            log.debug("Row:\n{}", row);
            return Optional.empty();
        }

        Integer year = Optional.ofNullable(
            row.cellStringValue(header.birthdayIndex())
        ).map(String::trim)
            .map(y -> Integer.parseInt(y.replaceFirst("\\.0", "")))
            .orElse(null);
        if (year == null) {
            log.trace("Year is not defined for '{}' for record #{}", name, row.index());
        }
        String region = Optional.ofNullable(
                row.cellStringValue(header.regionIndex())
        ).map(String::trim)
            .orElseGet(() -> {
                log.trace("Empty region:\n{}", row);
                return null;
            });
        if (region == null && year == null) {
            log.warn("Empty region and year for '{}'. Skip record #{}", name, row.index());
        }

        PlayerRank rank = Optional.ofNullable(
            row.cellStringValue(header.rankIndex())
        ).map(r -> {
            try {
                return PlayerRank.of(
                    r.trim().replaceFirst("\\.0", "")
                );
            } catch (IllegalArgumentException ex) {
                log.warn("{}, row:\n{}", ex.getMessage(), row);
                return null;
            }
        }).orElse(PlayerRank.NO_RANK);


        int rating = Optional.ofNullable(
            row.cellStringValue(header.scoreIndex())
        ).map(r -> r.trim().replaceFirst("\\.0", ""))
            .map(Integer::parseInt)
            .orElse(0);
        if (rating == 0) {
            log.trace("Empty rating:\n{}", row);
            log.warn("Rating value is not defined for '{}'. Skip record #{}", name, row.index());
            return Optional.empty();
        }

        return Optional.of(
            PersonalRating.builder()
                .player(
                    Player.builder()
                        .name(name)
                        .rank(rank)
                        .region(region)
                        .year(year)
                    .build()
                )
                .type(header.getPlayType())
                .ratingType(RatingType.RNBF)
                .value(rating)
            .build()
        );
    }

    boolean isData() {
        List<ExcelCell> cells = row.cells();
        if (cells.stream().filter(c -> !c.isBlank()).count() < Column.values().length) {
            return false;
        }
        if (header.isDetected()) {
            for (Column column : Column.values()) {
                ExcelCell cell = row.cell(header.index(column));
                if (!column.isOptional() && !isMatched(column, cell)) {
                    log.warn("Can't detect value for {} ({}): {}", column, header.getPlayType(), row);
                    return false;
                }
            }
            return true;
        } else {
            Map<Column, Integer> columnIndex = new HashMap<>();
            for (Column column : Column.values()) {
                for (ExcelCell cell : cells) {
                    if (column == SCORE
                        && (!columnIndex.containsKey(BIRTHDAY) || cell.index() <= columnIndex.get(BIRTHDAY))) {
                        continue;
                    }
                    if (column == RANK
                        && (!columnIndex.containsKey(NAME) || cell.index() <= columnIndex.get(NAME))) {
                        continue;
                    }
                    if (isMatched(column, cell)) {
                        columnIndex.put(column, cell.index());
                        log.trace("Matched: {}", column);
                        break;
                    }
                }
                if (!columnIndex.containsKey(column)) {
                    log.warn("Can't detect value for {} ({}): {}", column, header.getPlayType(), row);
                    return false;
                }
            }
            if (columnIndex.size() == Column.values().length) {
                header.updateColumnIndex(columnIndex);
                return true;
            }
        }
        return false;
    }

    private boolean isMatched(Column column, ExcelCell cell) {
        log.trace("Matching '{}' to '{}'", cell.stringValue(), column.getValuePattern().pattern());
        return column.getValuePattern().matcher(
            column == RANK
                ? cell.stringValue().toLowerCase()
                : cell.stringValue()
        ).find();
    }
}
