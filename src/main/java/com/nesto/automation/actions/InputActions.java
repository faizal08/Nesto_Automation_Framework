package com.nesto.automation.actions;

import com.nesto.automation.actions.WaitActions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Random;

public class InputActions {
    private WebDriver driver; // Added WebDriver reference for JavaScript execution
    private WaitActions waitActions;

    public InputActions(WebDriver driver, WaitActions waitActions) {
        this.driver = driver;
        this.waitActions = waitActions;
    }

    public void type(String xpath, String text) {
        // 1. Wait for the element to be visible
        WebElement element = waitActions.waitForElementVisible(xpath);

        // --- DYNAMIC DATA GENERATION LOGIC ---

        // Handle Random Email
        if (text.contains("{RANDOM_EMAIL}")) {
            long timestamp = System.currentTimeMillis();
            text = text.replace("{RANDOM_EMAIL}", "nesto_admin_" + timestamp + "@gmail.com");
            System.out.println("📧 Dynamic Email Generated: " + text);
        }

        // Handle Random Mobile Number (10 digits)
        if (text.contains("{RANDOM_MOBILE}")) {
            Random rand = new Random();
            long suffix = (long) (rand.nextDouble() * 100000000L);
            text = "9" + String.format("%08d", suffix);
            System.out.println("📱 Dynamic Mobile Generated: " + text);
        }

        // --- SPECIAL HANDLING FOR DATE PICKERS ---
        String typeAttribute = element.getAttribute("type");
        if (typeAttribute != null && typeAttribute.equalsIgnoreCase("date")) {
            System.out.println("📅 Date Input detected. Using JS to set value: " + text);
            try {
                JavascriptExecutor js = (JavascriptExecutor) driver;
                // Force the value into the date picker using YYYY-MM-DD format
                js.executeScript("arguments[0].value = '" + text + "';", element);

                // Trigger 'change' event so the application knows the date has been entered
                js.executeScript("arguments[0].dispatchEvent(new Event('change'));", element);
            } catch (Exception e) {
                System.err.println("❌ Failed to set date via JS: " + e.getMessage());
                element.sendKeys(text); // Fallback
            }
        } else {
            // Standard typing for normal text boxes
            element.clear();
            element.sendKeys(text);
        }

        System.out.println("⌨️ Action Completed on: " + xpath + " with value: " + text);
    }
}