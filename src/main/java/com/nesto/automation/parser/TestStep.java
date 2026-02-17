package com.nesto.automation.parser;


import lombok.Getter;
import lombok.Setter;

@Getter
public class TestStep {
    // --- Getters ---
    private String action;   // e.g., click, type, verify
    private String value;    // e.g., admin_user
    private String xpath;    // e.g., //input[@id='login']
    // --- Setters ---
    // NEW: This method allows TestExecutor to inject live data into the step
    // NEW: Getter for the report details
    @Setter
    private String details;  // NEW: Used to store comparison results (e.g., UI[10] vs DB[10])

    public TestStep(String action, String value, String xpath) {
        this.action = action;
        this.value = value;
        this.xpath = xpath;
        this.details = ""; // Initialize as empty string to avoid null pointers
    }

}