package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

class ExcelRow {
    private final Row row;

    ExcelRow(Row row) {

        this.row = row;
    }

    ExcelCell cell(int i) {
        return new ExcelCell(
            row.getCell(i)
        );
    }

    String cellStringValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.stringValue();

    }

    Double cellNumberValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.numberValue();
    }

    String cellUrlValue(Integer cellNum) {
        ExcelCell cell = new ExcelCell(row.getCell(cellNum));
        return cell.isBlank()
            ? null
            : cell.urlValue();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Cell cell : row) {
            result.append(
                String.format(
                    "[%d %7s] '%s'\n",
                    cell.getColumnIndex(),
                    cell.getCellType(),
                    (cell.toString().equals("Фото")
                        ? cell.getHyperlink().getAddress()
                        : cell)
                )
            );
        }
        return result.toString();
    }
}
