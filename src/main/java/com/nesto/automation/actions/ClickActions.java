package com.nesto.automation.actions;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class ClickActions {
    private WaitActions waitActions;

    public ClickActions(WebDriver driver, WaitActions waitActions) {
        this.waitActions = waitActions;
    }

    public void click(String xpath) {
        WebElement element = waitActions.waitForElementClickable(xpath);
        element.click();
        System.out.println("Successfully clicked: " + xpath);
    }
}
