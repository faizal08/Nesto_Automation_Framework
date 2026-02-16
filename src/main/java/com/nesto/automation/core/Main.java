package com.nesto.automation.core;

import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Define paths - Ensure this matches your actual file location
        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";

        // 2. Read raw strings from Excel
        ExcelReader reader = new ExcelReader();
        List<String> rawSteps = reader.getTestSteps(excelPath, "Sheet1");

        // 3. Parse raw strings into executable TestStep objects
        List<TestStep> executableSteps = new ArrayList<>();

        // Skip the header row (the one that says "Test Steps")
        boolean isHeader = true;
        for (String raw : rawSteps) {
            if (isHeader) {
                isHeader = false;
                continue;
            }

            // Only process rows that aren't empty
            if (raw != null && !raw.trim().isEmpty()) {
                // Convert the English sentence into an Action/Value/XPath object
                TestStep parsedStep = StepParser.parseStep(raw);
                executableSteps.add(parsedStep);
            }
        }

        // 4. Fire up the Selenium Engine and Run the Test!
        if (!executableSteps.isEmpty()) {
            TestExecutor executor = new TestExecutor();
            try {
                System.out.println("🎬 Starting Test Execution...");
                executor.runSteps(executableSteps);
                System.out.println("🏁 Test Execution Completed Successfully!");
            } catch (Exception e) {
                System.err.println("❌ Test Failed during execution: " + e.getMessage());
            } finally {
                // Uncomment the line below when you want the browser to close automatically
                // executor.quit();
            }
        } else {
            System.err.println("⚠️ No executable steps found! Check your Excel file and Column index.");
        }
    }
}