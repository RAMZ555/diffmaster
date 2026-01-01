package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.rules.ComparisonRules;

import java.io.StringReader;
import java.util.*;

/**
 * Properties file comparison.
 */
public class PropertiesComparator extends AbstractComparator {

    @Override
    public String getFormat() {
        return "properties";
    }

    @Override
    public boolean supports(String format) {
        return "properties".equalsIgnoreCase(format) || "props".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        try {
            Properties expProps = new Properties();
            Properties actProps = new Properties();

            expProps.load(new StringReader(expected));
            actProps.load(new StringReader(actual));

            compareProperties(expProps, actProps, rules);

            long duration = System.currentTimeMillis() - startTime;
            return buildResult("expected.properties", "actual.properties", duration);
        } catch (Exception e) {
            throw new ComparisonException("Failed to parse properties: " + e.getMessage(), e);
        }
    }

    private void compareProperties(Properties expected, Properties actual, ComparisonRules rules) {
        Set<String> allKeys = new TreeSet<>();
        expected.stringPropertyNames().forEach(allKeys::add);
        actual.stringPropertyNames().forEach(allKeys::add);

        for (String key : allKeys) {
            FieldPath keyPath = FieldPath.of(key);

            if (rules.shouldIgnore(keyPath, null, null)) {
                continue;
            }

            String expValue = expected.getProperty(key);
            String actValue = actual.getProperty(key);

            if (expValue == null) {
                recordAdded(keyPath, actValue);
            } else if (actValue == null) {
                recordRemoved(keyPath, expValue);
            } else if (rules.areValuesEqual(expValue, actValue)) {
                recordMatch(keyPath, expValue);
            } else {
                recordModified(keyPath, expValue, actValue);
            }
        }
    }
}
