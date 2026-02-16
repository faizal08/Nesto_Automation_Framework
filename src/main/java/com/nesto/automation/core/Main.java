package com.nesto.automation.core;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.nesto.automation.parser.StepParser;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.ExcelReader;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Setup Extent Reports Configuration
        String reportPath = System.getProperty("user.dir") + "/reports/Nesto_Automation_Report.html";
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("Nesto Automation Report");
        spark.config().setReportName("Nesto Admin Login Regression Results");

        ExtentReports extent = new ExtentReports();
        extent.attachReporter(spark);

        ExtentTest currentTest = null;
        String excelPath = "src/main/resources/testdata/Nesto_TestCases.xlsx";

        ExcelReader reader = new ExcelReader();
        List<String> rawSteps = reader.getTestSteps(excelPath, "Sheet1");
        List<TestStep> allSteps = new ArrayList<>();
        boolean isHeader = true;

        for (String raw : rawSteps) {
            if (isHeader) { isHeader = false; continue; }
            if (raw != null && !raw.trim().isEmpty()) {
                allSteps.add(StepParser.parseStep(raw));
            }
        }

        if (!allSteps.isEmpty()) {
            TestExecutor executor = new TestExecutor();
            try {
                System.out.println("🚀 Initializing Nesto Automation Engine...");
                int testCount = 0;

                for (TestStep step : allSteps) {

                    if (step.getAction().equalsIgnoreCase("openurl")) {

                        // FIX: Capture screenshot of the PREVIOUS test case before starting a new one
                        if (currentTest != null) {
                            String screenPath = executor.captureScreenshot("TC_" + testCount + "_Success");
                            currentTest.pass("Final State of TC #" + testCount,
                                    MediaEntityBuilder.createScreenCaptureFromPath(screenPath).build());
                        }

                        testCount++;
                        String testName = "Test Case #" + testCount;
                        currentTest = extent.createTest(testName);

                        System.out.println("\n--------------------------------------");
                        System.out.println("📝 RUNNING " + testName);
                        System.out.println("--------------------------------------");

                        executor.resetSession();
                        Thread.sleep(1000);
                        currentTest.info("Browser session reset and cookies cleared.");
                    }

                    try {
                        executor.executeIndividualStep(step);
                        if (currentTest != null) {
                            currentTest.pass("Step executed: " + step.getAction());
                        }
                    } catch (Exception stepException) {
                        if (currentTest != null) {
                            // Capture screenshot immediately on failure
                            String failurePath = executor.captureScreenshot("Failure_TC_" + testCount);
                            currentTest.fail("Step Failed: " + stepException.getMessage(),
                                    MediaEntityBuilder.createScreenCaptureFromPath(failurePath).build());
                        }
                        throw stepException;
                    }
                }

                // FIX: Capture the very last test case success screenshot
                if (currentTest != null) {
                    String lastImg = executor.captureScreenshot("TC_" + testCount + "_Success");
                    currentTest.pass("Final State of TC #" + testCount,
                            MediaEntityBuilder.createScreenCaptureFromPath(lastImg).build());
                }

                System.out.println("\n✅ ALL TESTS COMPLETED SUCCESSFULLY!");

            } catch (Exception e) {
                System.err.println("\n❌ EXECUTION STOPPED DUE TO ERROR: " + e.getMessage());
            } finally {
                extent.flush();
                System.out.println("📊 Detailed Report Generated at: " + reportPath);
                // executor.quit();
            }
        } else {
            System.err.println("⚠️ No executable steps found!");
        }
    }
}