package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.PlayerRank;
import com.github.bogdanovmn.badmrating.core.excel.ExcelCell;
import com.github.bogdanovmn.badmrating.core.excel.ExcelRow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
class ResultTableHeader {
    @Getter
    enum Column {
        NAME(false, "^\\s*\\p{Lu}\\p{L}+\\*?((\\s*|-)\\p{L}\\p{L}+)+\\s*\\.?\\s*$", "ФИО"),
        BIRTHDAY(true, "^\\s*\\d{4}(\\.0)?\\s*$", "Год/р"),
        RANK(
            true,
            "^\\s*("
                + String.join(
                "|",
            Arrays.stream(PlayerRank.values())
            .filter(pr -> pr != PlayerRank.NO_RANK)
            .flatMap(pr -> pr.getPossibleTitles().stream())
            .map(rankTitle -> rankTitle + "(\\.0)?")
            .toList())
            + ")\\s*$",
            "Разряд"
        ),
        REGION(true, "^\\s*\\p{L}{3}([\\\\/]\\p{L}{3})?\\s*$", "Регион"),
        SCORE(false, "^\\s*\\d+(\\.0)?\\s*$", "РС", "Рейтинг", "PC");

        private final boolean optional;
        private final Pattern valuePattern;
        private final Set<String> possibleTitles;

        Column(boolean optional, String valuePattern, String... possibleTitles) {
            this.optional = optional;
            this.valuePattern = Pattern.compile(valuePattern);
            this.possibleTitles = Arrays.stream(possibleTitles).collect(Collectors.toSet());
        }
    }

    private Map<Column, Integer> columnIndex;
    @Getter
    private final PlayType playType;

    void updateColumnIndex(Map<Column, Integer> index) {
        if (!isDetected()) {
            columnIndex = index;
            log.trace("{} Column index by data row: {}", playType, columnIndex);
        } else {
            throw new IllegalStateException("Column index is already defined: %s, new index: %s".formatted(columnIndex, index));
        }
    }
    boolean detect(ExcelRow row) {
        Map<Column, Integer> columnIndex = new HashMap<>();
        for (ExcelCell cell : row.cells()) {
            for (Column column : Column.values()) {
                if (column.possibleTitles.contains(cell.stringValue())) {
                    columnIndex.put(column, cell.index());
                    break;
                }
            }
        }
        if (columnIndex.size() == Column.values().length) {
            this.columnIndex = columnIndex;
            log.trace("Column index: {}", columnIndex);
            return true;
        }
        return false;
    }

    boolean isDetected() {
        return columnIndex != null;
    }

    int nameIndex() {
        return columnIndex.getOrDefault(Column.NAME, -1);
    }

    int birthdayIndex() {
        return columnIndex.getOrDefault(Column.BIRTHDAY, -1);
    }

    int rankIndex() {
        return columnIndex.getOrDefault(Column.RANK, -1);
    }

    int regionIndex() {
        return columnIndex.getOrDefault(Column.REGION, -1);
    }

    int scoreIndex() {
        return columnIndex.getOrDefault(Column.SCORE, -1);
    }

    int index(Column column) {
        return columnIndex.getOrDefault(column, -1);
    }
}
