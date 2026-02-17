package com.nesto.automation.core;

import com.nesto.automation.actions.*;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.DatabaseUtil;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestExecutor {
    private WebDriver driver;
    private ClickActions clickActions;
    private WaitActions waitActions;
    private InputActions inputActions;
    private VerificationActions verificationActions;
    private final String downloadPath;

    public TestExecutor() {
        WebDriverManager.chromedriver().setup();

        this.downloadPath = System.getProperty("user.dir") + File.separator + "downloads";
        File downloadDir = new File(downloadPath);
        if (!downloadDir.exists()) downloadDir.mkdirs();

        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);

        prefs.put("download.default_directory", downloadPath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--safebrowsing-disable-address-inventory-limit");
        options.addArguments("--disable-features=SafeBrowsingPasswordCheck");
        options.addArguments("--disable-sync");
        options.addArguments("--no-first-run");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        this.driver = new ChromeDriver(options);
        this.driver.manage().window().maximize();

        this.waitActions = new WaitActions(driver);
        this.clickActions = new ClickActions(driver, waitActions);
        this.inputActions = new InputActions(driver, waitActions);
        this.verificationActions = new VerificationActions(driver, waitActions);
    }

    public void executeIndividualStep(TestStep step) {
        String action = step.getAction().toLowerCase();
        String xpath = step.getXpath();
        String value = step.getValue();

        System.out.println("🚀 Executing: " + action + " | Value: " + value + " | XPath: " + xpath);

        switch (action) {
            case "openurl":
                driver.get(value);
                break;

            case "click":
                if (xpath != null && (xpath.contains("submit") || value.toLowerCase().contains("sign in"))) {
                    waitActions.waitForElementClickable(xpath).submit();
                } else {
                    try {
                        WebElement el = waitActions.waitForElementClickable(xpath);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    } catch (Exception e) { clickActions.click(xpath); }
                }
                break;

            case "type":
                inputActions.type(xpath, value);
                break;

            case "dbexecute":
                if (value.contains("{DB_QUERY}")) {
                    String sql = value.replace("{DB_QUERY}", "").trim();
                    DatabaseUtil.executeUpdate(sql);
                    step.setDetails("DB Action Executed: " + sql);
                } else {
                    throw new RuntimeException("❌ dbexecute requires {DB_QUERY} prefix in Value column");
                }
                break;

            case "verify":
            case "verifydownload":
                // 1. URL or PDF Verification (No XPath provided)
                if (xpath == null || xpath.isEmpty() || xpath.trim().equalsIgnoreCase("null")) {
                    if (value.toLowerCase().contains(".pdf") || action.equals("verifydownload")) {
                        handleFileVerification(value);
                    } else {
                        // If it's a URL check
                        boolean urlMatched = waitActions.waitForUrl(value);
                        if (!urlMatched) throw new RuntimeException("❌ URL Verification Failed: " + value);
                    }
                } else {
                    // 2. Data/Count Verification (XPath exists)
                    String expectedValue = value;

                    // --- DATABASE FETCH LOGIC ---
                    if (value.startsWith("{DB_QUERY}")) {
                        String sql = value.replace("{DB_QUERY}", "").trim();
                        System.out.println("🔍 Querying Database: " + sql);
                        expectedValue = DatabaseUtil.getSingleValue(sql);

                        if (expectedValue == null || expectedValue.equals("DB_ERROR")) {
                            throw new RuntimeException("❌ Database Error: Query returned no value or failed. Query: " + sql);
                        }
                    }

                    // --- UI VALIDATION LOGIC ---
                    if (xpath.endsWith("/tr") || xpath.endsWith("/tbody/tr") || xpath.toLowerCase().contains("count")) {
                        // COUNT VERIFICATION
                        List<WebElement> elements = driver.findElements(By.xpath(xpath));
                        String actualCount = String.valueOf(elements.size());
                        String res = "Count Validation: UI[" + actualCount + "] vs DB/Expected[" + expectedValue + "]";
                        step.setDetails(res);
                        if (!actualCount.equals(expectedValue.trim())) throw new RuntimeException("❌ COUNT MISMATCH! " + res);
                        System.out.println("✅ PASS: " + res);
                    } else {
                        // TEXT VERIFICATION
                        WebElement element = waitActions.waitForElementVisible(xpath);
                        String actualUIValue = element.getText().trim();
                        String res = "Text Validation: UI[" + actualUIValue + "] vs DB/Expected[" + expectedValue + "]";
                        step.setDetails(res);

                        // Using case-insensitive contains for better reliability
                        if (actualUIValue.toLowerCase().contains(expectedValue.toLowerCase().trim())) {
                            System.out.println("✅ PASS: " + res);
                        } else {
                            throw new RuntimeException("❌ DATA MISMATCH! " + res);
                        }
                    }
                }
                break;

            default:
                System.out.println("⚠️ Unknown action: " + action);
        }
    }

    private void handleFileVerification(String extension) {
        System.out.println("⏳ Waiting 5s for download...");
        try { Thread.sleep(5000); } catch (InterruptedException e) {}
        File dir = new File(downloadPath);
        File[] files = dir.listFiles();
        boolean found = false;
        if (files != null) {
            for (File f : files) {
                if (f.getName().toLowerCase().contains(extension.toLowerCase().replace(".pdf",""))) {
                    found = true;
                    f.delete();
                    break;
                }
            }
        }
        if (!found) throw new RuntimeException("❌ Download Failed!");
    }

    public void resetSession() { if (driver != null) driver.manage().deleteAllCookies(); }

    public String captureScreenshot(String fileName) {
        File folder = new File("reports/screenshots");
        if (!folder.exists()) folder.mkdirs();
        String path = "reports/screenshots/" + fileName + ".png";
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File(path));
        } catch (IOException e) { e.printStackTrace(); }
        return "screenshots/" + fileName + ".png";
    }

    public void quit() { if (driver != null) driver.quit(); }
}