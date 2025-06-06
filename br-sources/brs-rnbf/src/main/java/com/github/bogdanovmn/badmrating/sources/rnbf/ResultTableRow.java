package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import com.github.bogdanovmn.badmrating.core.excel.ExcelCell;
import com.github.bogdanovmn.badmrating.core.excel.ExcelRow;
import com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.BIRTHDAY;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.NAME;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.RANK;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.REGION;
import static com.github.bogdanovmn.badmrating.sources.rnbf.ResultTableHeader.Column.SCORE;

@RequiredArgsConstructor
@Slf4j
class ResultTableRow {
    private final ResultTableHeader header;
    private final ExcelRow row;

    Optional<PersonalRating> fetch() {
        String name = Optional.ofNullable(
            row.cellStringValue(header.nameIndex())
        ).map(n -> {
            String newName = new HumanNameInputString(n).normalized(header.getPlayType().getSex());
            if (!newName.equals(n)) {
                log.debug("Normalize {} name '{}' to '{}'", header.getPlayType().getSex(), n, newName);
            }
            return newName;
        }).orElse(null);

        if (name == null) {
            log.warn("Can't find proper name. Skip record #{}", row.index());
            log.debug("Row:\n{}", row);
            return Optional.empty();
        }

        Integer year = Optional.ofNullable(
            row.cellDateValue(header.birthdayIndex())
        ).map(LocalDateTime::getYear)
            .orElseGet(
                () -> Optional.ofNullable(
                    row.cellStringValue(header.birthdayIndex())
                ).map(y -> {
                    try {
                        return Integer.parseInt(y.trim().replaceFirst("!", "1"));
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                }).orElse(null)
            );
        if (year == null) {
            log.trace("Year is not defined for '{}' for record #{}", name, row.index());
        }
        String region = Optional.ofNullable(
            row.cellStringValue(header.regionIndex())
        ).map(String::trim)
            .filter(r -> isMatched(REGION, r))
            .orElseGet(() -> {
                log.trace("Empty region:\n{}", row);
                return null;
            });
        if (region == null && year == null) {
            log.debug("Empty region and year for '{}'. Skip record #{}", name, row.index());
            return Optional.empty();
        }

        PlayerRank rank = Optional.ofNullable(
            row.cellStringValue(header.rankIndex())
        ).map(r -> {
            try {
                return PlayerRank.of(
                    r.trim().replaceFirst("\\.0", "")
                );
            } catch (IllegalArgumentException ex) {
                log.warn(ex.getMessage());
                return null;
            }
        }).orElse(PlayerRank.NO_RANK);


        Integer rating = Optional.ofNullable(
            row.cellStringValue(header.scoreIndex())
        ).map(r -> Math.round(Float.parseFloat(r.trim())))
            .orElse(null);
        if (rating == null) {
            log.warn("Rating value is not defined for '{}'. Skip record #{}", name, row.index());
            return Optional.empty();
        }
        if (rating == 0) {
            log.debug("Rating value for '{}' is 0. Skip record #{}", name, row.index());
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
                .type(
                    header.getPlayType() == PlayType.WXD || header.getPlayType() == PlayType.MXD
                        ? PlayType.XD
                        : header.getPlayType()
                )
                .value(rating)
            .build()
        );
    }

    boolean isData() {
        List<ExcelCell> cells = row.cells();
        if (row.notEmptyValues() < Column.values().length) {
            return false;
        }
        if (header.isDetected()) {
            for (Column column : Column.values()) {
                ExcelCell cell = row.cell(header.index(column));
                String value = cell.stringValue();
                if (!column.isOptional() && !isMatched(column, value)) {
                    log.warn("Value for {} ({}#row#{}) is not matched: '{}'", column, header.getPlayType(), row.index(), value);
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
                    if (isMatched(column, cell.stringValue())) {
                        columnIndex.put(column, cell.index());
                        log.trace("Matched: {}", column);
                        break;
                    }
                }
                if (!columnIndex.containsKey(column)) {
                    log.debug("Can't detect value for {} ({}): {}", column, header.getPlayType(), row);
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

    private boolean isMatched(Column column, String cellValue) {
        log.trace("Matching '{}' to '{}'", cellValue, column.getValuePattern().pattern());
        return column.getValuePattern().matcher(
            column == RANK
                ? cellValue.toLowerCase()
                : cellValue
        ).find();
    }
}
