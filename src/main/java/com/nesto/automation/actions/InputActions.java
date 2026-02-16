package com.nesto.automation.actions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

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

        // 3. Type the new text
        element.sendKeys(text);

        System.out.println("⌨️ Typed '" + text + "' into: " + xpath);
    }
}
