package com.nesto.automation.actions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class WaitActions {
    private WebDriverWait wait;

    public WaitActions(WebDriver driver) {
        // Set a global 10-second timeout
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebElement waitForElementVisible(String xpath) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    public WebElement waitForElementClickable(String xpath) {
        return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
    }

    public void waitForUrl(String url) {
        wait.until(org.openqa.selenium.support.ui.ExpectedConditions.urlToBe(url));
    }
}