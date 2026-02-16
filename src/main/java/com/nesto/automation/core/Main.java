package com.nesto.automation.core;

import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Define paths (Adjust this to your actual Excel file location)
        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";

        // 2. Read raw strings from Excel
        ExcelReader reader = new ExcelReader();
        List<String> rawSteps = reader.getTestSteps(excelPath, "Sheet1");

        // 3. Parse raw strings into executable TestStep objects
        List<TestStep> executableSteps = new ArrayList<>();
        boolean isHeader = true;
        for (String raw : rawSteps) {
            if (isHeader) {
                isHeader = false; // 👈 Skip the first row (Header)
                continue;
            }
            if (raw != null && !raw.trim().isEmpty()) {
                executableSteps.add(StepParser.parseStep(raw));
            }
        }

        // 4. Fire up the Engine and Run!
        TestExecutor executor = new TestExecutor();
        try {
            executor.runSteps(executableSteps);
        } finally {
            // Ensure browser closes even if test fails
           // executor.quit();
        }
    }
}
