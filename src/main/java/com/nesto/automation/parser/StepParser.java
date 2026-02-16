package com.nesto.automation.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StepParser {

    public static TestStep parseStep(String rawStep) {
        String action = "unknown";
        String xpath = "";
        String value = "";

        if (rawStep == null || rawStep.trim().isEmpty()) {
            return new TestStep(action, value, xpath);
        }

        String lowerStep = rawStep.toLowerCase();

        // 1. Identify Action
        if (lowerStep.contains("open url")) {
            action = "openurl";
        } else if (lowerStep.contains("click")) {
            action = "click"; // 👈 Check for CLICK first!
        } else if (lowerStep.contains("verify")) {
            action = "verify";
        } else if (lowerStep.contains("type") || lowerStep.contains("enter")) {
            action = "type";
        }

        // 2. Extract XPath (Look for // inside DOUBLE quotes only)
        // This regex ignores single quotes so it won't break on [@type='submit']
        Pattern xpathPattern = Pattern.compile("[\"“](//.*?)[\"”]");
        Matcher xpathMatcher = xpathPattern.matcher(rawStep);
        if (xpathMatcher.find()) {
            xpath = xpathMatcher.group(1).trim();
        }

        // 3. Extract Value (The first quoted string that is NOT the XPath)
        Pattern valuePattern = Pattern.compile("[\"“]([^\"“”]*)[\"”]");
        Matcher valueMatcher = valuePattern.matcher(rawStep);
        while (valueMatcher.find()) {
            String found = valueMatcher.group(1).trim();
            if (!found.startsWith("//") && !found.isEmpty() && value.isEmpty()) {
                value = found;
            }
        }

        return new TestStep(action, value, xpath);
    }
}
