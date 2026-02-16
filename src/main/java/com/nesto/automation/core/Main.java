package com.nesto.automation.core;

import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Define path to your Excel file
        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";

        // 2. Read raw strings from Excel
        ExcelReader reader = new ExcelReader();
        List<String> rawSteps = reader.getTestSteps(excelPath, "Sheet1");

        // 3. Parse raw strings into executable TestStep objects
        List<TestStep> allSteps = new ArrayList<>();
        boolean isHeader = true;

        for (String raw : rawSteps) {
            // Skip the first row if it is a header (e.g., "Test Steps")
            if (isHeader) {
                isHeader = false;
                continue;
            }

            // Only add steps that are not empty
            if (raw != null && !raw.trim().isEmpty()) {
                allSteps.add(StepParser.parseStep(raw));
            }
        }

        // 4. Fire up the Selenium Engine and Run the Tests
        if (!allSteps.isEmpty()) {
            TestExecutor executor = new TestExecutor();
            try {
                System.out.println("🚀 Initializing Nesto Automation Engine...");

                int testCount = 0;
                for (TestStep step : allSteps) {

                    // Logic: Every time we see "openurl", we treat it as a brand new Test Case
                    if (step.getAction().equalsIgnoreCase("openurl")) {
                        testCount++;
                        System.out.println("\n--------------------------------------");
                        System.out.println("📝 RUNNING TEST CASE #" + testCount);
                        System.out.println("--------------------------------------");

                        // IMPORTANT: Clear cookies and wait a moment.
                        // This ensures TC_01 success doesn't bypass the login screen for TC_02.
                        executor.resetSession();
                        Thread.sleep(1000);
                    }

                    // Run the specific step (Type, Click, or Verify)
                    executor.executeIndividualStep(step);
                }

                System.out.println("\n✅ ALL TESTS COMPLETED SUCCESSFULLY!");

            } catch (Exception e) {
                System.err.println("\n❌ EXECUTION STOPPED DUE TO ERROR:");
                System.err.println("👉 " + e.getMessage());
                // Use e.printStackTrace() if you need to see the exact line number of the crash
                // e.printStackTrace();
            } finally {
                // To keep the browser open for inspection, keep this commented out.
                // To close it automatically, uncomment the line below:
                // executor.quit();
            }
        } else {
            System.err.println("⚠️ No executable steps found! Check your Excel file and Column index (should be Column 2/C).");
        }
    }
}