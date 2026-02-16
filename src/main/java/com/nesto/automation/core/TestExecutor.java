package com.nesto.automation.core;

import com.nesto.automation.actions.ClickActions;
import com.nesto.automation.actions.InputActions;
import com.nesto.automation.actions.VerificationActions;
import com.nesto.automation.actions.WaitActions;
import com.nesto.automation.parser.TestStep;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;

public class TestExecutor {
    private WebDriver driver;
    private ClickActions clickActions;
    private WaitActions waitActions;
    private InputActions inputActions;
    private VerificationActions verificationActions;

    public TestExecutor() {
        // 1. Setup the Driver automatically
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();
        this.driver.manage().window().maximize();

        // 2. Initialize the Action helpers
        this.waitActions = new WaitActions(driver);
        this.clickActions = new ClickActions(driver, waitActions);
        this.inputActions = new InputActions(driver, waitActions);
        this.verificationActions = new VerificationActions(driver, waitActions);
    }

    public void runSteps(List<TestStep> steps) {
        for (TestStep step : steps) {
            executeStep(step);
        }
    }

    private void executeStep(TestStep step) {
        String action = step.getAction();
        String xpath = step.getXpath();
        String value = step.getValue();

        System.out.println("🚀 Executing: " + action + " | Value: " + value + " | XPath: " + xpath);

        switch (action.toLowerCase()) {
            case "openurl":
                driver.get(value);
                break;

            case "click":
                clickActions.click(xpath);
                break;

            case "type":
                inputActions.type(xpath, value);
                break;

            case "verify":
                if (xpath.isEmpty()) {
                    // If no XPath is provided (like in our new Step 5), verify the URL
                    verificationActions.verifyUrl(value);
                } else {
                    // If XPath is provided, verify the text in that element
                    verificationActions.verifyText(xpath, value);
                }
                break;

            // We will add 'type' and 'verify' cases as we build those action classes
            default:
                System.out.println("⚠️ Unknown action: " + action);
        }
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}
