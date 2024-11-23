package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.Player;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import com.github.bogdanovmn.badmrating.core.RatingType;
import com.github.bogdanovmn.badmrating.core.excel.ExcelCell;
import com.github.bogdanovmn.badmrating.core.excel.ExcelRow;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
class ResultTable {
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BIRTHDAY = "birthday";
    private static final String COLUMN_RANK = "rank";
    private static final String COLUMN_REGION = "region";
    private static final String COLUMN_RS = "rs";

    private static final Collection<Head> COLUMNS = List.of(
        new Head(COLUMN_NAME, "^\\s*\\p{L}+(\\s+\\p{L}+)+\\s*\\.?\\s*$", "ФИО"),
        new Head(COLUMN_BIRTHDAY, "^\\s*\\d{4}(\\.0)?\\s*$", "Год/р"),
        new Head(
            COLUMN_RANK,
            "^\\s*("
                + String.join(
                    "|",
                    Arrays.stream(PlayerRank.values())
                        .filter(pr -> pr != PlayerRank.NO_RANK)
                        .flatMap(pr -> pr.getPossibleTitles().stream())
                        .map(rankTitle -> rankTitle + "(\\.0)?")
                        .toList())
                + ")\\s*$",
            "Разряд"),
        new Head(COLUMN_REGION, "^\\s*\\p{L}{3}([\\\\/]\\p{L}{3})?\\s*$", "Регион"),
        new Head(COLUMN_RS, "^\\s*\\d+(\\.0)?\\s*$", "РС", "PC", "Рейтинг")
    );

    private final List<ExcelRow> rows;
    private final PlayType playType;

    private Map<String, Integer> columnIndex;

    @Value
    private static class Head {
        String id;
        Pattern valuePattern;
        Set<String> possibleTitles;

        Head(String id, String valuePattern, String... possibleTitles) {
            this.id = id;
            this.valuePattern = Pattern.compile(valuePattern);
            this.possibleTitles = Arrays.stream(possibleTitles).collect(Collectors.toSet());
        }
    }

    public List<PersonalRating> ratings() {
        List<PersonalRating> result = new ArrayList<>();
        for (ExcelRow row : rows) {
            log.trace("Row to process:\n{}", row);
            if (isHeadDetected()) {
                if (isData(row)) {
                    fetchData(row).ifPresent(result::add);
                }
            } else if (isHeader(row)) {
                continue;
            } else if (isData(row)) {
                fetchData(row).ifPresent(result::add);
            }
        }
        if (!isHeadDetected()) {
            throw new IllegalStateException("Can't detect head row");
        }
        return result;
    }

    private Optional<PersonalRating> fetchData(ExcelRow row) {
        String name = Optional.ofNullable(
            row.cellStringValue(columnIndex.get(COLUMN_NAME))
            ).map(n ->
                n.trim()
                    .replaceAll("\\s+?", " ")
                    .replaceAll("\\P{L}", "")
            ).orElse(null);
        if (name == null) {
            log.warn("Can't find proper name. Skip record #{}", row.index());
            log.debug("Row:\n{}", row);
            return Optional.empty();
        }

        int year = Optional.ofNullable(
            row.cellStringValue(columnIndex.get(COLUMN_BIRTHDAY))
        ).map(String::trim)
            .map(y -> Integer.parseInt(y.replaceFirst("\\.0", "")))
            .orElse(0);
        if (year == 0) {
            log.warn("Year is not defined for '{}'. Skip record #{}", name, row.index());
            return Optional.empty();
        }

        PlayerRank rank = PlayerRank.of(
            row.cellStringValue(columnIndex.get(COLUMN_RANK)).trim()
                .replaceFirst("\\.0", "")
        );

        String region = row.cellStringValue(columnIndex.get(COLUMN_REGION)).trim();

        int rating = Integer.parseInt(
            row.cellStringValue(columnIndex.get(COLUMN_RS)).trim().replaceFirst("\\.0", "")
        );

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
                .type(playType)
                .ratingType(RatingType.RNBFRating)
                .value(rating)
            .build()
        );
    }

    private boolean isData(ExcelRow row) {
        List<ExcelCell> cells = row.cells();
        if (cells.stream().filter(c -> !c.isBlank()).count() < COLUMNS.size()) {
            return false;
        }
        Map<String, Integer> columnIndex = new HashMap<>();
        for (Head column : COLUMNS) {
            for (ExcelCell cell : cells) {
                if (column.id.equals(COLUMN_RS) && cell.index() <= columnIndex.get(COLUMN_BIRTHDAY)) {
                    continue;
                }
                if (column.id.equals(COLUMN_RANK) && cell.index() <= columnIndex.get(COLUMN_NAME)) {
                    continue;
                }
                log.trace("Matching '{}' to '{}'", cell.stringValue(), column.valuePattern.pattern());
                if (column.valuePattern.matcher(cell.stringValue()).find()) {
                    columnIndex.put(column.id, cell.index());
                    log.trace("Matched: {}", column.id);
                    break;
                }
            }
            if (!columnIndex.containsKey(column.id)) {
                log.warn("Can't detect value for {} ({}):\n{}", column.id, playType, row);
                return false;
            }
        }
        if (columnIndex.size() == COLUMNS.size()) {
            if (!isHeadDetected()) {
                this.columnIndex = columnIndex;
                log.debug("{} Column index by data row: {}", playType, columnIndex);
            }
            return true;
        }
        return false;
    }

    private boolean isHeader(ExcelRow row) {
        Map<String, Integer> columnIndex = new HashMap<>();
        for (ExcelCell cell : row.cells()) {
            for (Head head : COLUMNS) {
                if (head.possibleTitles.contains(cell.stringValue())) {
                    columnIndex.put(head.id, cell.index());
                    break;
                }
            }
        }
        if (columnIndex.size() == COLUMNS.size()) {
            this.columnIndex = columnIndex;
            log.debug("{} Column index: {}", playType, columnIndex);
            return true;
        }
        return false;
    }

    private boolean isHeadDetected() {
        return columnIndex != null;
    }
}
