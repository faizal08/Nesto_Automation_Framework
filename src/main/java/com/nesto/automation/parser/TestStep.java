package com.nesto.automation.parser;

public class TestStep {
    private String action;   // e.g., click, type, verify
    private String value;    // e.g., admin_user
    private String xpath;    // e.g., //input[@id='login']

    public TestStep(String action, String value, String xpath) {
        this.action = action;
        this.value = value;
        this.xpath = xpath;
    }

    // Getters
    public String getAction() { return action; }
    public String getValue() { return value; }
    public String getXpath() { return xpath; }
}
