package com.nesto.automation.core;

import com.nesto.automation.actions.*;
import com.nesto.automation.parser.TestStep;
import com.nesto.automation.utils.DatabaseUtil; // Added Import
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
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

        // --- 🔒 PASSWORD MANAGER & AUTOFILL BLOCKERS ---
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);

        // --- 📥 AUTO-DOWNLOAD SETTINGS (Prevents "Save As" Popup) ---
        prefs.put("download.default_directory", downloadPath);
        prefs.put("download.prompt_for_download", false);
        prefs.put("download.directory_upgrade", true);
        prefs.put("safebrowsing.enabled", true);
        options.setExperimentalOption("prefs", prefs);

        // --- 🛡️ BROWSER SECURITY ---
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

            case "verify":
            case "verifydownload":
                if (xpath == null || xpath.isEmpty()) {
                    if (value.toLowerCase().contains("pdf")) {
                        handleFileVerification(value);
                    } else {
                        boolean urlMatched = waitActions.waitForUrl(value);
                        if (!urlMatched) throw new RuntimeException("❌ URL Verification Failed: " + value);
                    }
                } else {
                    // --- 📊 NEW: LIVE DATABASE VERIFICATION LOGIC ---
                    String expectedValue = value;
                    if (value.startsWith("{DB_QUERY}")) {
                        String sql = value.replace("{DB_QUERY}", "");
                        expectedValue = DatabaseUtil.getSingleValue(sql);
                        System.out.println("🔍 DB Result: " + expectedValue);
                    }

                    // Get UI text and compare
                    WebElement element = waitActions.waitForElementVisible(xpath);
                    String actualUIValue = element.getText().trim();

                    if (actualUIValue.contains(expectedValue)) {
                        System.out.println("✅ PASS: UI (" + actualUIValue + ") matches DB (" + expectedValue + ")");
                    } else {
                        throw new RuntimeException("❌ DATA MISMATCH! UI shows: " + actualUIValue + " but DB says: " + expectedValue);
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
                if (f.getName().toLowerCase().contains(extension.toLowerCase())) {
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
        try { FileUtils.copyFile(((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE), new File(path));
        } catch (IOException e) { e.printStackTrace(); }
        return "screenshots/" + fileName + ".png";
    }
    public void quit() { if (driver != null) driver.quit(); }
}