package com.nesto.automation.actions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VerificationActions {
    private WaitActions waitActions;
    private WebDriver driver;

    public VerificationActions(WebDriver driver, WaitActions waitActions) {
        this.driver = driver;
        this.waitActions = waitActions;
    }

    public void verifyText(String xpath, String expectedText) {
        // 1. Wait for the element to appear
        WebElement element = waitActions.waitForElementVisible(xpath);

        // 2. Get the actual text from the website
        String actualText = element.getText().trim();

        // 3. Compare them
        if (actualText.equalsIgnoreCase(expectedText)) {
            System.out.println("✅ PASS: Found expected text [" + expectedText + "]");
        } else {
            throw new RuntimeException("❌ FAIL: Expected [" + expectedText + "] but found [" + actualText + "]");
        }
    }


    public void verifyElementDisplayed(String xpath) {
        WebElement element = waitActions.waitForElementVisible(xpath);
        if (element.isDisplayed()) {
            System.out.println("✅ PASS: Element is visible on screen.");
        }
    }

    public void verifyUrl(String expectedUrl) {
        try {
            // 1. Give the browser time to redirect
            waitActions.waitForUrl(expectedUrl);
            System.out.println("✅ PASS: URL matches [" + expectedUrl + "]");
        } catch (Exception e) {
            // 2. If it still doesn't match after waiting, then fail
            String actualUrl = driver.getCurrentUrl();
            throw new RuntimeException("❌ FAIL: Expected URL [" + expectedUrl + "] but was [" + actualUrl + "]");
        }
    }
}
