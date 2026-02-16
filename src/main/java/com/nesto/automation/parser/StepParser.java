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

        // 1. Identify Action (Check for 'click' before 'type' to avoid confusion)
        if (lowerStep.contains("open url")) {
            action = "openurl";
        } else if (lowerStep.contains("click")) {
            action = "click";
        } else if (lowerStep.contains("verify")) {
            action = "verify";
        } else if (lowerStep.contains("type") || lowerStep.contains("enter")) {
            action = "type";
        }

        // 2. Extract XPath
        // Looks for content starting with // inside double quotes or smart quotes
        Pattern xpathPattern = Pattern.compile("[\"“](//.*?)[\"”]");
        Matcher xpathMatcher = xpathPattern.matcher(rawStep);
        if (xpathMatcher.find()) {
            xpath = xpathMatcher.group(1).trim();
        }

        // 3. Extract Value
        // Logic: Find strings inside quotes that do NOT start with //
        // We added support for single quotes 'value' just in case.
        Pattern valuePattern = Pattern.compile("[\"“']([^\"“”']*)[\"”']");
        Matcher valueMatcher = valuePattern.matcher(rawStep);
        while (valueMatcher.find()) {
            String found = valueMatcher.group(1).trim();

            // If the found text is NOT the XPath we already found, it must be the Value
            if (!found.equals(xpath) && !found.startsWith("//") && !found.isEmpty() && value.isEmpty()) {
                value = found;
            }
        }

        return new TestStep(action, value, xpath);
    }
}
