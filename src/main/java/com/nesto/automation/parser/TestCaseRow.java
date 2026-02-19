package com.nesto.automation.parser;

public class TestCaseRow {
    public String tcId;
    public String tcDesc;
    public String stepText;

    public TestCaseRow(String tcId, String tcDesc, String stepText) {
        this.tcId = tcId;
        this.tcDesc = tcDesc;
        this.stepText = stepText;
    }
}
