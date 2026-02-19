package com.nesto.automation.utils;

import com.nesto.automation.parser.TestCaseRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public List<TestCaseRow> getSheetData(Workbook workbook, int sheetIndex) {
        List<TestCaseRow> dataList = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(sheetIndex);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Read raw values
            String tcId = (row.getCell(0) != null) ? row.getCell(0).toString().trim() : "";
            String tcDesc = (row.getCell(1) != null) ? row.getCell(1).toString().trim() : "";
            String rawStep = (row.getCell(2) != null) ? row.getCell(2).toString().trim() : "";

            if (rawStep.isEmpty()) continue;

            // 🔥 CLEANING CODE: Strip "1.", "2. " etc.
            String cleanedStep = rawStep.replaceFirst("^\\d+\\.\\s*", "");

            // Package it up
            dataList.add(new TestCaseRow(tcId, tcDesc, cleanedStep));
        }
        return dataList;
    }
}