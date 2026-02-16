package com.nesto.automation.actions;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class ClickActions {
    private WaitActions waitActions;
    private WebDriver driver;

    public ClickActions(WebDriver driver, WaitActions waitActions) {
        this.driver = driver;
        this.waitActions = waitActions;
    }

    public void click(String xpath) {
        WebElement element = waitActions.waitForElementClickable(xpath);
        try {
            // Standard click
            element.click();
        } catch (Exception e) {
            // Force click if standard fails
            System.out.println("⚠️ Click intercepted, forcing via JavaScript...");
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}