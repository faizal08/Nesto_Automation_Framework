package com.nesto.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class VerificationActions {
    private WaitActions waitActions;
    private WebDriver driver;

    public VerificationActions(WebDriver driver, WaitActions waitActions) {
        this.driver = driver;
        this.waitActions = waitActions;
    }

    /**
     * Verifies if the element text contains the expected text (Case-Insensitive)
     */
    public void verifyText(String xpath, String expectedText) {
        WebElement element = waitActions.waitForElementVisible(xpath);
        String actualText = element.getText().trim();

        if (actualText.toLowerCase().contains(expectedText.toLowerCase().trim())) {
            System.out.println("✅ PASS: Text validation successful! Found: [" + actualText + "]");
        } else {
            throw new RuntimeException("❌ FAIL: Text mismatch! Expected [" + expectedText + "] but got [" + actualText + "]");
        }
    }

    /**
     * Verifies if the Current URL contains the expected keyword (e.g., 'dashboard' or 'error')
     */
    public void verifyUrl(String expectedUrlPart) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // This is the CRITICAL part: Wait for the address bar to change
            wait.until(ExpectedConditions.urlContains(expectedUrlPart));
            System.out.println("✅ PASS: URL now contains [" + expectedUrlPart + "]");
        } catch (Exception e) {
            throw new RuntimeException("❌ FAIL: URL did not change to [" + expectedUrlPart + "] within 10s");
        }
    }

    public void verifyElementCount(String xpath, String expectedCount) {
        int actualCount = driver.findElements(By.xpath(xpath)).size();
        if (String.valueOf(actualCount).equals(expectedCount.trim())) {
            System.out.println("✅ PASS: Count matches! Found " + actualCount + " rows.");
        } else {
            throw new RuntimeException("❌ FAIL: Count mismatch! UI has [" + actualCount + "] rows but DB expected [" + expectedCount + "]");
        }
    }
}