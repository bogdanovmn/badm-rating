package com.github.bogdanovmn.badmrating.core.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class ExcelFile implements Closeable {
    private final Workbook excelBook;
    private final Map<String, Sheet> sheets;

    public ExcelFile(InputStream source) throws IOException {
        excelBook = WorkbookFactory.create(source);
        sheets = new HashMap<>();
        for (int i = 0; i < excelBook.getNumberOfSheets(); i++) {
            if (excelBook.getSheetVisibility(i) == SheetVisibility.VISIBLE) {
                sheets.put(excelBook.getSheetName(i), excelBook.getSheetAt(i));
            } else {
                log.warn("Skipping hidden sheet {}", excelBook.getSheetName(i));
            }
        }
    }

    public Set<String> sheets() {
        return sheets.keySet();
    }

    public List<ExcelRow> sheetByName(String name) {
        Sheet sheet = sheets.get(name);
        List<ExcelRow> rows = new ArrayList<>();
        sheet.rowIterator().forEachRemaining(
            r -> rows.add(new ExcelRow(r))
        );
        return rows;
    }

    @Override
    public void close() throws IOException {
        excelBook.close();
    }
}
