package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExcelCell {
    private final Cell cell;

    ExcelCell(Cell cell) {
        this.cell = cell;
    }

    boolean isFormula() {
        return cell.getCellType().equals(CellType.FORMULA);
    }

    boolean isContainString(String value) {
        return cell.getCellType().equals(CellType.STRING)
            && cell.getStringCellValue().contains(value);
    }

    boolean isLike(Pattern pattern) {
        return cell.getCellType().equals(CellType.STRING)
            && pattern.matcher(
            cell.getStringCellValue()
        ).find();
    }

    List<Integer> getSumRange() {
        Matcher m = Pattern.compile("^SUM\\(\\w(\\d+):\\w(\\d+)\\)$").matcher(cell.toString());
        if (m.find()) {
            return Arrays.asList(
                Integer.parseInt(m.group(1)),
                Integer.parseInt(m.group(2))
            );
        }
        throw new IllegalStateException(
            String.format(
                "Cell '%s' parse SUM() range error", cell
            )
        );
    }

    boolean isNumber() {
        return !isBlank() && cell.getCellType().equals(CellType.NUMERIC);
    }

    boolean isString() {
        return cell.getCellType().equals(CellType.STRING);
    }

    boolean isBlank() {
        return cell == null || cell.getCellType().equals(CellType.BLANK);

    }

    String stringValue() {
        return isString()
            ? cell.getStringCellValue()
            : "";
    }

    double numberValue() {
        return cell.getNumericCellValue();
    }

    String urlValue() {
        Hyperlink hyperlink = cell.getHyperlink();
        return hyperlink == null
            ? null
            : hyperlink.getAddress();
    }
}
