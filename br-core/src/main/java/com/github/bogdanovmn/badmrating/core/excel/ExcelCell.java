package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Hyperlink;

import java.time.LocalDateTime;
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
        if (isString()) {
            return cell.getStringCellValue().replaceAll("\\p{Zs}+", " ");
        } else if (isNumber()) {
            return String.valueOf(cell.getNumericCellValue());
        } else if (isFormula()) {
            try {
                return String.valueOf(cell.getNumericCellValue());
            } catch (IllegalStateException e) {
                return cell.getStringCellValue();
            }
        } else {
            return "";
        }
    }

    public double numberValue() {
        return cell.getNumericCellValue();
    }

    public LocalDateTime dateValue() {
        if (isNumber()) {
            double year = cell.getNumericCellValue();
            if (year <= 2999) {
                return LocalDateTime.of((int) year, 1, 1, 0, 0);
            } else {
                try {
                    return cell.getLocalDateTimeCellValue();
                } catch (IllegalStateException e) {
                    return null;
                }
            }
        }
        return null;
    }

    public String urlValue() {
        Hyperlink hyperlink = cell.getHyperlink();
        return hyperlink == null
            ? null
            : hyperlink.getAddress();
    }
}
