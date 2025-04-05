package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExcelRow {
    private final Row row;

    public ExcelRow(Row row) {
        this.row = row;
    }

    public int index() {
        return row.getRowNum();
    }

    public ExcelCell cell(int i) {
        return new ExcelCell(
            row.getCell(i)
        );
    }

    public String cellStringValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.stringValue();
    }

    public LocalDateTime cellDateValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.dateValue();
    }

    public Double cellNumberValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.numberValue();
    }

    public String cellUrlValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.urlValue();
    }

    public List<ExcelCell> cells() {
        List<ExcelCell> result = new ArrayList<>();
        for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                result.add(new ExcelCell(cell));
            }
        }
        return result;
    }

    public long notEmptyValues() {
        return cells().stream()
            .map(ExcelCell::stringValue)
            .filter(v -> v != null && !v.isBlank())
            .count();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Cell cell : row) {
            result.append(
                String.format(
                    "[%d %7s] '%s' ",
                    cell.getColumnIndex(),
                    cell.getCellType(),
                    new ExcelCell(cell).stringValue()
                )
            );
        }
        return result.toString();
    }
}
