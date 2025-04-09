package com.github.bogdanovmn.badmrating.sources.rnbf;

import com.github.bogdanovmn.badmrating.core.PersonalRating;
import com.github.bogdanovmn.badmrating.core.PlayType;
import com.github.bogdanovmn.badmrating.core.excel.ExcelRow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
class ResultTable {
    private final List<ExcelRow> rows;
    private final PlayType playType;

    public List<PersonalRating> ratings() {
        List<PersonalRating> result = new ArrayList<>();
        ResultTableHeader header = new ResultTableHeader(playType);
        int processedRows = 0;
        int emptyRows = 0;
        for (ExcelRow rawRow : rows) {
            log.trace("Row to process:\n{}", rawRow);
            processedRows++;
            ResultTableRow row = new ResultTableRow(header, rawRow);
            if (header.isDetected()) {
                if (row.isData()) {
                    row.fetch().ifPresent(result::add);
                } else if (emptyRows++ > 3) {
                    break;
                }
            } else if (header.detect(rawRow)) {
                continue;
            } else if (row.isData()) {
                row.fetch().ifPresent(result::add);
            }
        }
        log.trace("Processed rows: {}", processedRows);
        if (!header.isDetected()) {
            throw new IllegalStateException("Can't detect head row");
        }
        return result;
    }
}
