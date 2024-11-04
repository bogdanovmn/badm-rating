package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExcelCell {
    private final Cell cell;

    public ExcelCell(Cell cell) {
        this.cell = cell;
    }

    public int index() {
        return cell.getColumnIndex();
    }

    public boolean isFormula() {
        return cell.getCellType().equals(CellType.FORMULA);
    }

    public boolean isContainString(String value) {
        return cell.getCellType().equals(CellType.STRING)
            && cell.getStringCellValue().contains(value);
    }

    public boolean isLike(Pattern pattern) {
        return cell.getCellType().equals(CellType.STRING)
            && pattern.matcher(
            cell.getStringCellValue()
        ).find();
    }

    public List<Integer> getSumRange() {
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

    public boolean isNumber() {
        return !isBlank() && cell.getCellType().equals(CellType.NUMERIC);
    }

    public boolean isString() {
        return cell.getCellType().equals(CellType.STRING);
    }

    public boolean isBlank() {
        return cell == null || cell.getCellType().equals(CellType.BLANK);

    }

    public String stringValue() {
        return isString()
            ? cell.getStringCellValue()
            : isNumber()
                ? String.valueOf(cell.getNumericCellValue())
                : "";
    }

    public double numberValue() {
        return cell.getNumericCellValue();
    }

    public String urlValue() {
        Hyperlink hyperlink = cell.getHyperlink();
        return hyperlink == null
            ? null
            : hyperlink.getAddress();
    }
}
