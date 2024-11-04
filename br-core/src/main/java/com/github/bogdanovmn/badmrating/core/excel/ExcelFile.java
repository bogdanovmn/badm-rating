package com.github.bogdanovmn.badmrating.core.excel;

import org.apache.poi.ss.usermodel.Sheet;
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


public class ExcelFile implements Closeable {
    private final Workbook excelBook;
    private final Map<String, Sheet> sheets;

    public ExcelFile(InputStream source) throws IOException {
        excelBook = WorkbookFactory.create(source);
        sheets = new HashMap<>();
        for (int i = 0; i < excelBook.getNumberOfSheets(); i++) {
            sheets.put(excelBook.getSheetName(i), excelBook.getSheetAt(i));
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
