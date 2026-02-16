package com.nesto.automation.core;

import com.nesto.automation.actions.*;
import com.nesto.automation.parser.TestStep;
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

    public TestExecutor() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // --- 🔒 RESTORED: PASSWORD MANAGER & AUTOFILL BLOCKERS ---
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);

        // --- 🛡️ RESTORED: SAFE BROWSING & BREACH CHECK BLOCKERS ---
        options.addArguments("--safebrowsing-disable-address-inventory-limit");
        options.addArguments("--safebrowsing-disable-extension-whitelist");
        options.addArguments("--disable-features=SafeBrowsingPasswordCheck");
        options.addArguments("--disable-sync");
        options.addArguments("--no-first-run");

        // --- 🛠️ RESTORED: AUTOMATION INFOBAR REMOVAL ---
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        this.driver = new ChromeDriver(options);
        this.driver.manage().window().maximize();

        this.waitActions = new WaitActions(driver);
        this.clickActions = new ClickActions(driver, waitActions);
        this.inputActions = new InputActions(driver, waitActions);
        this.verificationActions = new VerificationActions(driver, waitActions);
    }

    public void resetSession() {
        if (driver != null) {
            try {
                driver.manage().deleteAllCookies();
                System.out.println("🧹 Session cleared: Cookies deleted.");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to clear cookies: " + e.getMessage());
            }
        }
    }

    public void executeIndividualStep(TestStep step) {
        String action = step.getAction();
        String xpath = step.getXpath();
        String value = step.getValue();

        System.out.println("🚀 Executing: " + action + " | Value: " + value + " | XPath: " + xpath);

        switch (action.toLowerCase()) {
            case "openurl":
                driver.get(value);
                break;

            case "click":
                // 1. Logic for Login Form Submission
                if (xpath.contains("submit") || value.toLowerCase().contains("sign in") || value.toLowerCase().contains("sign up")) {
                    WebElement element = waitActions.waitForElementClickable(xpath);
                    element.submit();
                    System.out.println("📤 Form submitted via .submit()");
                } else {
                    // 2. Logic for Sidebar/Links - Using JS Click to bypass overlapping layers
                    try {
                        WebElement element = waitActions.waitForElementClickable(xpath);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                        System.out.println("🖱️ Clicked via JavaScript (Bypassed UI blocks)");
                    } catch (Exception e) {
                        clickActions.click(xpath); // Fallback
                    }
                }
                break;

            case "type":
                inputActions.type(xpath, value);
                break;

            case "verify":
                if (xpath == null || xpath.isEmpty()) {
                    // Optimized for Multi-Sheet: Handles "Verify URL contains"
                    boolean urlMatched = waitActions.waitForUrl(value);
                    if (!urlMatched) {
                        throw new RuntimeException("❌ URL Verification Failed! Expected to find: " + value);
                    }
                    System.out.println("✅ PASS: URL correctly contains [" + value + "]");
                } else {
                    verificationActions.verifyText(xpath, value);
                }
                break;

            default:
                System.out.println("⚠️ Unknown action keyword: " + action);
        }
    }

    public String captureScreenshot(String fileName) {
        File folder = new File("reports/screenshots");
        if (!folder.exists()) folder.mkdirs();

        String filePath = "reports/screenshots/" + fileName + ".png";
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(src, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "screenshots/" + fileName + ".png";
    }

    public void quit() {
        if (driver != null) driver.quit();
    }
}