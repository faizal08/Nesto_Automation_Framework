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
        } else if (lowerStep.contains("dbexecute")) {
            action = "dbexecute";
        }

        // 2. Extract XPath (Improved to support (//xpath)[index])
        Pattern xpathPattern = Pattern.compile("[\"“](\\(?//.*?)[\"”]");
        Matcher xpathMatcher = xpathPattern.matcher(rawStep);
        if (xpathMatcher.find()) {
            xpath = xpathMatcher.group(1).trim();
        }

        // 3. Handle Special Case: Verification without XPath (URL or PDF checks)
        // If it's a verify action but no XPath was found, we flag it so the Executor doesn't crash
        if (action.equals("verify") && xpath.isEmpty()) {
            if (lowerStep.contains("url") || lowerStep.contains(".pdf") || lowerStep.contains("invoice")) {
                xpath = "SKIP_XPATH";
            }
        }

        // 4. Extract Value
        Pattern valuePattern = Pattern.compile("[\"“]([^\"“”]+)[\"”]");
        Matcher valueMatcher = valuePattern.matcher(rawStep);

        while (valueMatcher.find()) {
            String found = valueMatcher.group(1).trim();

            // If it's not the XPath, and doesn't look like an XPath, it's our Value
            if (!found.equals(xpath) && !found.startsWith("//") && !found.startsWith("(//") && value.isEmpty()) {
                value = found;
            }
        }

        // 5. Fallback for OpenURL which might not have quotes
        if (action.equals("openurl") && value.isEmpty()) {
            int urlIndex = lowerStep.indexOf("url");
            if (urlIndex != -1) {
                value = rawStep.substring(urlIndex + 3).trim().replace("\"", "");
            }
        }

        // 6. Special Fix for DB_QUERY steps
        if (value.isEmpty() && rawStep.contains("{DB_QUERY}")) {
            int start = rawStep.indexOf("{DB_QUERY}");
            // Find the end by looking for the closing quote after the query
            int end = rawStep.indexOf("\"", start);
            if (end == -1) end = rawStep.length();

            value = rawStep.substring(start, end).trim();
        }

        return new TestStep(action, value, xpath);
    }
}