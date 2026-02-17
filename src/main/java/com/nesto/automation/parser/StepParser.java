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
            action = "click";
        } else if (lowerStep.contains("verify")) {
            action = "verify";
        } else if (lowerStep.contains("type") || lowerStep.contains("enter")) {
            action = "type";
        }

        // 2. Extract XPath (Look for the part starting with // inside quotes)
        Pattern xpathPattern = Pattern.compile("[\"“](//.*?)[\"”]");
        Matcher xpathMatcher = xpathPattern.matcher(rawStep);
        if (xpathMatcher.find()) {
            xpath = xpathMatcher.group(1).trim();
        }

        // 3. Extract Value (Improved Logic)
        // We look for content inside double quotes specifically.
        // This regex now IGNORES single quotes so it won't break on SQL queries.
        Pattern valuePattern = Pattern.compile("[\"“]([^\"“”]+)[\"”]");
        Matcher valueMatcher = valuePattern.matcher(rawStep);

        while (valueMatcher.find()) {
            String found = valueMatcher.group(1).trim();

            // If it's not the XPath and not empty, it's our Value
            if (!found.equals(xpath) && !found.startsWith("//") && value.isEmpty()) {
                value = found;
            }
        }

        // Fallback for OpenURL which might not have quotes
        if (action.equals("openurl") && value.isEmpty()) {
            value = rawStep.substring(rawStep.toLowerCase().indexOf("url") + 3).trim();
        }

        return new TestStep(action, value, xpath);
    }
}