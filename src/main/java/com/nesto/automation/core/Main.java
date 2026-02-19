package com.nesto.automation.core;

import com.aventstack.extentreports.*;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestCaseRow;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Setup Reports
        String reportPath = System.getProperty("user.dir") + "/reports/Nesto_Automation_Report.html";
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Nesto Automation Report");
        spark.config().setReportName("Nesto Multi-Sheet Test Execution");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);

        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";
        TestExecutor executor = new TestExecutor();
        ExcelReader reader = new ExcelReader();
        ExtentTest currentTest = null;

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            System.out.println("🚀 Initializing Nesto Multi-Sheet Automation Engine...");

            // --- OUTER LOOP: Iterate through all Sheets ---
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                String moduleName = workbook.getSheetName(s);
                System.out.println("\n📂 PROCESSING MODULE: [" + moduleName + "]");

                // 🔥 Let ExcelReader handle the extraction and "1." cleaning
                List<TestCaseRow> allRows = reader.getSheetData(workbook, s);

                // --- INNER LOOP: Iterate through the cleaned rows ---
                for (int i = 0; i < allRows.size(); i++) {
                    TestCaseRow currentRow = allRows.get(i);

                    // Whenever a new TC ID is found, start a NEW Report entry
                    if (!currentRow.tcId.isEmpty()) {

                        // ✅ RESTORED: Success screenshot for the previous test case
                        if (currentTest != null && currentTest.getModel().getStatus() == Status.PASS) {
                            String screen = executor.captureScreenshot("Pass_" + System.currentTimeMillis());
                            currentTest.pass("Test Case Completed Successfully", MediaEntityBuilder.createScreenCaptureFromPath(screen).build());
                        }

                        String fullTestTitle = "[" + moduleName + "] " + currentRow.tcId + " : " + currentRow.tcDesc;
                        currentTest = extent.createTest(fullTestTitle);

                        System.out.println("📝 Running: " + currentRow.tcId);

                        // Reset session only for the first sheet (Auth_Tests)
                        if (s == 0) {
                            executor.resetSession();
                        }
                    }

                    // Execute and Log Step
                    try {
                        TestStep parsedStep = StepParser.parseStep(currentRow.stepText);

                        // 1. Execute the logic
                        executor.executeIndividualStep(parsedStep);

                        // 2. Log the basic step text (Cleaned by ExcelReader)
                        currentTest.pass(currentRow.stepText);

                        // 3. ✅ RESTORED: Enhanced Reporting for DB/UI Details
                        String extraDetails = parsedStep.getDetails();
                        if (extraDetails != null && !extraDetails.isEmpty()) {
                            currentTest.info("<span style='color:#00e5ff; font-weight:bold;'>🔍 Data Verification: " + extraDetails + "</span>");
                        }

                    } catch (Exception stepException) {
                        // Log failure and take screenshot
                        String failScreen = executor.captureScreenshot("Fail_" + System.currentTimeMillis());
                        currentTest.fail("❌ FAILED at step: " + currentRow.stepText + " | Error: " + stepException.getMessage(),
                                MediaEntityBuilder.createScreenCaptureFromPath(failScreen).build());

                        System.err.println("⚠️ TC Failed. Skipping remaining steps for this test case...");

                        // ✅ RESTORED: Skip logic using the List index
                        while (i + 1 < allRows.size() && allRows.get(i + 1).tcId.isEmpty()) {
                            i++;
                        }
                    }
                }
            }

            // ✅ RESTORED: Final success screenshot at the end of all modules
            if (currentTest != null && currentTest.getModel().getStatus() == Status.PASS) {
                String lastImg = executor.captureScreenshot("Final_Execution_Success");
                currentTest.pass("All modules completed successfully.", MediaEntityBuilder.createScreenCaptureFromPath(lastImg).build());
            }

            System.out.println("\n✅ ENGINE FINISHED ALL MODULES!");

        } catch (Exception e) {
            System.err.println("\n❌ FATAL SYSTEM ERROR: " + e.getMessage());
        } finally {
            extent.flush();
            executor.quit();
            System.out.println("📊 Detailed Report Generated at: " + reportPath);
        }
    }
}