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
import java.util.ArrayList;
import java.util.List;

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

                // Whenever a new TC ID is found, finalize previous and start a NEW Report entry
                if (!tcId.isEmpty()) {

                    // Capture screenshot for previous test before starting new one
                    if (currentTest != null) {
                        String screen = executor.captureScreenshot("Success_" + System.currentTimeMillis());
                        currentTest.pass("Final State", MediaEntityBuilder.createScreenCaptureFromPath(screen).build());
                    }

                    // Create New Test in Report with ID and Description
                    String fullTestTitle = tcId + " : " + tcDesc;
                    currentTest = extent.createTest(fullTestTitle);

                    System.out.println("\n--------------------------------------");
                    System.out.println("📝 RUNNING: " + fullTestTitle);
                    System.out.println("--------------------------------------");

                    executor.resetSession();
                }

                // Execute and Log Step
                try {
                    TestStep parsedStep = StepParser.parseStep(stepText);
                    executor.executeIndividualStep(parsedStep);
                    currentTest.pass(stepText);
                } catch (Exception stepException) {
                    String failScreen = executor.captureScreenshot("Fail_" + tcId);
                    currentTest.fail("Failed at step: " + stepText + " | Error: " + stepException.getMessage(),
                            MediaEntityBuilder.createScreenCaptureFromPath(failScreen).build());
                    break; // Stop steps for this specific TC on failure
                }
            }

            // Final screenshot for the very last test case
            if (currentTest != null) {
                String lastImg = executor.captureScreenshot("Final_Success");
                currentTest.pass("All steps completed.", MediaEntityBuilder.createScreenCaptureFromPath(lastImg).build());
            }

            workbook.close();
            fis.close();
            System.out.println("\n✅ ALL TESTS COMPLETED SUCCESSFULLY!");

        } catch (Exception e) {
            System.err.println("\n❌ FATAL ERROR: " + e.getMessage());
        } finally {
            extent.flush();
            System.out.println("📊 Detailed Report Generated at: " + reportPath);
        }
    }
}