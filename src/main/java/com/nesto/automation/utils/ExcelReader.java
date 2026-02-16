package com.nesto.automation.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {

    /**
     * Reads test steps from a specific Excel sheet.
     * Now updated to handle Column C (Index 2) and strip step numbers.
     */
    public List<String> getTestSteps(String filePath, String sheetName) {
        List<String> steps = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                System.err.println("❌ Error: Sheet '" + sheetName + "' not found in " + filePath);
                return steps;
            }

            for (Row row : sheet) {
                // CHANGED: Index is now 2 (Column C)
                Cell cell = row.getCell(2);

                if (cell != null && cell.getCellType() == CellType.STRING) {
                    String rawText = cell.getStringCellValue().trim();

                    if (!rawText.isEmpty()) {
                        // NEW: Clean the step text
                        // This regex replaces "1.", "2. ", "10." at the start of a string
                        String cleanedText = rawText.replaceFirst("^\\d+\\.\\s*", "");

                        steps.add(cleanedText);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error reading Excel: " + e.getMessage());
        }
        return steps;
    }
}