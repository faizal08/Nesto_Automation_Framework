package com.nesto.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Random;

public class InputActions {
    private WaitActions waitActions;

    public InputActions(WebDriver driver, WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    public void type(String xpath, String text) {
        // 1. Wait for the box to be visible
        WebElement element = waitActions.waitForElementVisible(xpath);

        // 2. Clear any existing text
        element.clear();

        // --- DYNAMIC DATA GENERATION LOGIC ---

        // Handle Random Email
        if (text.contains("{RANDOM_EMAIL}")) {
            long timestamp = System.currentTimeMillis();
            text = text.replace("{RANDOM_EMAIL}", "nesto_admin_" + timestamp + "@gmail.com");
            System.out.println("📧 Dynamic Email Generated: " + text);
        }

        // Handle Random Mobile Number (10 digits)
        if (text.contains("{RANDOM_MOBILE}")) {
            // Generates a random number starting with 9, 8, or 7
            Random rand = new Random();
            long suffix = (long) (rand.nextDouble() * 100000000L); // 8 digits
            text = "9" + String.format("%08d", suffix);
            System.out.println("📱 Dynamic Mobile Generated: " + text);
        }

        // 3. Type the (potentially modified) text
        element.sendKeys(text);

        System.out.println("⌨️ Typed '" + text + "' into: " + xpath);
    }
}
