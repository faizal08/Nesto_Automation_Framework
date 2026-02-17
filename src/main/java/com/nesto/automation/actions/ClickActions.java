package com.nesto.automation.actions;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ClickActions {
    private WaitActions waitActions;
    private WebDriver driver;

    public ClickActions(WebDriver driver, WaitActions waitActions) {
        this.driver = driver;
        this.waitActions = waitActions;
    }

    /**
     * Clicks an element and immediately handles browser confirmation popups
     */
    public void click(String xpath) {
        // 1. Find the element
        WebElement element = waitActions.waitForElementClickable(xpath);

        try {
            // 2. Perform the click
            element.click();
        } catch (Exception e) {
            // Fallback for overlapping elements
            System.out.println("⚠️ Standard click failed, using JavaScript for: " + xpath);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }

        // 3. IMMEDIATELY check for the "Are you sure?" popup
        handleConfirmationAlert();
    }

    /**
     * Specifically designed to catch and accept 'confirm' dialogs instantly
     */
    private void handleConfirmationAlert() {
        try {
            // We use a very short timeout (500ms) because alerts appear instantly.
            // This prevents the "slowness" you noticed earlier.
            WebDriverWait quickWait = new WebDriverWait(driver, Duration.ofMillis(500));
            Alert alert = quickWait.until(ExpectedConditions.alertIsPresent());

            String alertText = alert.getText();
            alert.accept(); // Clicks "OK"

            System.out.println("🔔 Auto-Accepted Popup: [" + alertText + "]");
        } catch (TimeoutException e) {
            // No alert present - this is the expected result for 90% of clicks
        } catch (NoAlertPresentException e) {
            // Secondary safety check
        }
    }
}