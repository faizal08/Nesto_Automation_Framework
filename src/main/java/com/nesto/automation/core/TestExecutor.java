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

        // 1. Disable the Password Manager & Autofill Service
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("autofill.profile_enabled", false);
        // This specifically targets the "Breach/Leaked" check
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);

        // 2. Disable Safe Browsing (This is why it keeps re-enabling)
        // These arguments stop the background service that checks passwords
        options.addArguments("--safebrowsing-disable-address-inventory-limit");
        options.addArguments("--safebrowsing-disable-extension-whitelist");
        options.addArguments("--disable-features=SafeBrowsingPasswordCheck");

        // 3. Prevent Chrome from "Syncing" or searching for your Google Account
        options.addArguments("--disable-sync");
        options.addArguments("--no-first-run");

        // 4. Clean up Automation info bars
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
                // Handle Login Form Submission
                if (xpath.contains("submit") || value.toLowerCase().contains("sign in")) {
                    WebElement element = waitActions.waitForElementClickable(xpath);
                    element.submit();
                    System.out.println("📤 Form submitted via .submit()");
                } else {
                    // Use JavaScript click for the Register Link to bypass any overlapping UI layers
                    try {
                        WebElement element = waitActions.waitForElementClickable(xpath);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
                        System.out.println("🖱️ Clicked via JavaScript (Bypassed UI blocks)");
                    } catch (Exception e) {
                        clickActions.click(xpath); // Fallback to standard click
                    }
                }
                break;

            case "type":
                inputActions.type(xpath, value);
                break;

            case "verify":
                if (xpath == null || xpath.isEmpty()) {
                    // Wait for URL change (Crucial for TC_03)
                    boolean urlChanged = waitActions.waitForUrl(value);
                    if (urlChanged) {
                        System.out.println("✅ PASS: URL correctly contains [" + value + "]");
                    }
                } else {
                    verificationActions.verifyText(xpath, value);
                }
                break;

            default:
                System.out.println("⚠️ Unknown action keyword: " + action);
        }
    }

    public String captureScreenshot(String fileName) {
        // 1. Ensure folder exists
        File folder = new File("reports/screenshots");
        if (!folder.exists()) folder.mkdirs();

        // 2. We save the file to the physical path
        String filePath = "reports/screenshots/" + fileName + ".png";

        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(src, new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3. CRITICAL: Return ONLY the relative path for the HTML report to read
        return "screenshots/" + fileName + ".png";
    }

    public void quit() {
        if (driver != null) driver.quit();
    }
}