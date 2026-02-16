package com.nesto.automation.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;
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
        spark.config().setReportName("Nesto Detailed Test Execution Results");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);

        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";
        TestExecutor executor = new TestExecutor();
        ExtentTest currentTest = null;

        try {
            FileInputStream fis = new FileInputStream(excelPath);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet sheet = workbook.getSheet("Sheet1");

            System.out.println("🚀 Initializing Nesto Automation Engine...");

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Column 0 = ID, Column 1 = Description, Column 2 = Test Steps
                String tcId = (row.getCell(0) != null) ? row.getCell(0).toString().trim() : "";
                String tcDesc = (row.getCell(1) != null) ? row.getCell(1).toString().trim() : "";
                String stepText = (row.getCell(2) != null) ? row.getCell(2).toString().trim() : "";

                if (stepText.isEmpty()) continue;

                // Whenever a new TC ID is found, start a NEW Report entry
                if (!tcId.isEmpty()) {
                    // Capture screenshot for previous test success before starting new one
                    if (currentTest != null && currentTest.getModel().getStatus().toString().equalsIgnoreCase("pass")) {
                        String screen = executor.captureScreenshot("Final_" + System.currentTimeMillis());
                        currentTest.pass("Final State Success", MediaEntityBuilder.createScreenCaptureFromPath(screen).build());
                    }

                    String fullTestTitle = tcId + " : " + tcDesc;
                    currentTest = extent.createTest(fullTestTitle);

                    System.out.println("\n--------------------------------------");
                    System.out.println("📝 RUNNING: " + fullTestTitle);
                    System.out.println("--------------------------------------");

                    executor.resetSession();
                }

                // 2. Execute and Log Step with Error Isolation
                try {
                    TestStep parsedStep = StepParser.parseStep(stepText);
                    executor.executeIndividualStep(parsedStep);
                    currentTest.pass(stepText);
                } catch (Exception stepException) {
                    // Log the failure
                    String failScreen = executor.captureScreenshot("Fail_" + System.currentTimeMillis());
                    currentTest.fail("❌ FAILED at step: " + stepText + " | Error: " + stepException.getMessage(),
                            MediaEntityBuilder.createScreenCaptureFromPath(failScreen).build());

                    System.err.println("⚠️ TC Failed. Skipping remaining steps for this test case...");

                    // 3. SKIP LOGIC: Move the index 'i' to the end of this test case
                    // We check if the next row's ID column is empty. If empty, it belongs to the current failed TC.
                    while (i + 1 <= sheet.getLastRowNum()) {
                        Row nextRow = sheet.getRow(i + 1);
                        if (nextRow != null) {
                            String nextId = (nextRow.getCell(0) != null) ? nextRow.getCell(0).toString().trim() : "";
                            if (nextId.isEmpty()) {
                                i++; // Skip this row because it's a step of the failed test
                            } else {
                                break; // Found the next Test Case ID, exit the skip-loop
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

            // Final screenshot if the last test was successful
            if (currentTest != null && currentTest.getModel().getStatus().toString().equalsIgnoreCase("pass")) {
                String lastImg = executor.captureScreenshot("Final_Success");
                currentTest.pass("All steps completed.", MediaEntityBuilder.createScreenCaptureFromPath(lastImg).build());
            }

            workbook.close();
            fis.close();
            System.out.println("\n✅ ENGINE FINISHED ALL TEST CASES!");

        } catch (Exception e) {
            System.err.println("\n❌ FATAL SYSTEM ERROR: " + e.getMessage());
        } finally {
            extent.flush();
            System.out.println("📊 Detailed Report Generated at: " + reportPath);
        }
    }
}