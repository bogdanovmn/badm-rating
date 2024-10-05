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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
class ResultTable {
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_BIRTHDAY = "birthday";
    private static final String COLUMN_RANK = "rank";
    private static final String COLUMN_REGION = "region";
    private static final String COLUMN_RS = "rs";

    private static final Set<Head> COLUMNS = Set.of(
        new Head(COLUMN_NAME, "ФИО"),
        new Head(COLUMN_BIRTHDAY, "Год/р"),
        new Head(COLUMN_RANK, "Разряд"),
        new Head(COLUMN_REGION, "Регион"),
        new Head(COLUMN_RS, "РС", "Рейтинг")
    );

    private final Iterator<ExcelRow> rows;
    private final PlayType playType;

    private Map<String, Integer> columnIndex;

    @Value
    private static class Head {
        String id;
        Set<String> possibleTitles;

        Head(String id, String... possibleTitles) {
            this.id = id;
            this.possibleTitles = Arrays.stream(possibleTitles).collect(Collectors.toSet());
        }
    }

    public PersonalRating nextRecord() {
        while (rows.hasNext() && !isHeadDetected()) {
            detectHeadRow(rows.next());
        }
        if (!isHeadDetected()) {
            throw new IllegalStateException("Can't detect head row");
        }

        if (!rows.hasNext()) {
            return null;
        }

        ExcelRow row = rows.next();
        log.trace("Row to process:\n{}", row);
        return PersonalRating.builder()
            .player(
                Player.builder()
                    .name(
                        row.cellStringValue(columnIndex.get(COLUMN_NAME))
                    )
                    .rank(
                        PlayerRank.of(
                            row.cellStringValue(columnIndex.get(COLUMN_RANK))
                        )
                    )
                    .region(
                        row.cellStringValue(columnIndex.get(COLUMN_REGION))
                    )
                    .year(
                        Integer.parseInt(
                            row.cellStringValue(columnIndex.get(COLUMN_BIRTHDAY))
                        )
                    )
                .build()
            )
            .type(playType)
            .ratingType(RatingType.RNBFRating)
            .value(
                row.cellNumberValue(columnIndex.get(COLUMN_RS)).intValue()
            )
        .build();
    }


    private void detectHeadRow(ExcelRow row) {
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
            log.info("Column index: {}", columnIndex);
        }
    }

    private boolean isHeadDetected() {
        return columnIndex != null;
    }
}
