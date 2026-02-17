package com.nesto.automation.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;

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
        ExtentTest currentTest = null;

        try (FileInputStream fis = new FileInputStream(excelPath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            System.out.println("🚀 Initializing Nesto Multi-Sheet Automation Engine...");

            // --- OUTER LOOP: Iterate through all Sheets ---
            for (int s = 0; s < workbook.getNumberOfSheets(); s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String moduleName = sheet.getSheetName();

                System.out.println("\n📂 PROCESSING MODULE: [" + moduleName + "]");

                // --- INNER LOOP: Iterate through Rows in the current sheet ---
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    String tcId = (row.getCell(0) != null) ? row.getCell(0).toString().trim() : "";
                    String tcDesc = (row.getCell(1) != null) ? row.getCell(1).toString().trim() : "";
                    String stepText = (row.getCell(2) != null) ? row.getCell(2).toString().trim() : "";

                    if (stepText.isEmpty()) continue;

                    // Whenever a new TC ID is found, start a NEW Report entry
                    if (!tcId.isEmpty()) {
                        // Success screenshot for the previous test case
                        if (currentTest != null && currentTest.getModel().getStatus() == Status.PASS) {
                            String screen = executor.captureScreenshot("Pass_" + System.currentTimeMillis());
                            currentTest.pass("Test Case Completed Successfully", MediaEntityBuilder.createScreenCaptureFromPath(screen).build());
                        }

                        String fullTestTitle = "[" + moduleName + "] " + tcId + " : " + tcDesc;
                        currentTest = extent.createTest(fullTestTitle);

                        System.out.println("📝 Running: " + tcId);

                        // Reset session only for the first sheet (Auth_Tests)
                        if (s == 0) {
                            executor.resetSession();
                        }
                    }

                    // Execute and Log Step
                    try {
                        TestStep parsedStep = StepParser.parseStep(stepText);

                        // 1. Execute the logic (Selenium + Database)
                        executor.executeIndividualStep(parsedStep);

                        // 2. Log the basic step text
                        currentTest.pass(stepText);

                        // 3. --- 📊 ENHANCED REPORTING: Capture DB/UI Details ---
                        String extraDetails = parsedStep.getDetails();
                        if (extraDetails != null && !extraDetails.isEmpty()) {
                            // Injects the live data comparison as a blue info block in Extent Report
                            currentTest.info("<span style='color:#00e5ff; font-weight:bold;'>🔍 Data Verification: " + extraDetails + "</span>");
                        }

                    } catch (Exception stepException) {
                        // Log failure and take screenshot
                        String failScreen = executor.captureScreenshot("Fail_" + System.currentTimeMillis());
                        currentTest.fail("❌ FAILED at step: " + stepText + " | Error: " + stepException.getMessage(),
                                MediaEntityBuilder.createScreenCaptureFromPath(failScreen).build());

                        System.err.println("⚠️ TC Failed. Skipping remaining steps for this test case...");

                        // SKIP LOGIC: Move pointer to the next Test Case ID
                        while (i + 1 <= sheet.getLastRowNum()) {
                            Row nextRow = sheet.getRow(i + 1);
                            if (nextRow != null) {
                                String nextId = (nextRow.getCell(0) != null) ? nextRow.getCell(0).toString().trim() : "";
                                if (nextId.isEmpty()) {
                                    i++;
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            }

            // Final success screenshot
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