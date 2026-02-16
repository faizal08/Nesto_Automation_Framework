package com.nesto.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    public List<String> getTestSteps(String filePath, String sheetName) {
        List<String> steps = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            for (Row row : sheet) {
                // Assuming Test Steps are in Column 4 (index 4) based on your file
                Cell cell = row.getCell(4);
                if (cell != null) {
                    steps.add(cell.getStringCellValue());
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading Excel: " + e.getMessage());
        }
        return steps;
    }
}
