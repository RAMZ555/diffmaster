package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.rules.ComparisonRules;

import java.util.*;

/**
 * Text line-by-line comparison.
 */
public class TextComparator extends AbstractComparator {

    @Override
    public String getFormat() {
        return "text";
    }

    @Override
    public boolean supports(String format) {
        return "text".equalsIgnoreCase(format) || "txt".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        String[] expLines = expected.split("\\r?\\n", -1);
        String[] actLines = actual.split("\\r?\\n", -1);

        if (rules.isIgnoreArrayOrder()) {
            compareLinesUnordered(expLines, actLines, rules);
        } else {
            compareLinesOrdered(expLines, actLines, rules);
        }

        long duration = System.currentTimeMillis() - startTime;
        return buildResult("expected.txt", "actual.txt", duration);
    }

    private void compareLinesOrdered(String[] expected, String[] actual, ComparisonRules rules) {
        int maxLines = Math.max(expected.length, actual.length);

        for (int i = 0; i < maxLines; i++) {
            FieldPath linePath = FieldPath.of("line").index(i + 1);

            if (i >= expected.length) {
                recordAdded(linePath, actual[i]);
                continue;
            }

            if (i >= actual.length) {
                recordRemoved(linePath, expected[i]);
                continue;
            }

            String expLine = expected[i];
            String actLine = actual[i];

            if (rules.areValuesEqual(expLine, actLine)) {
                recordMatch(linePath, expLine);
            } else {
                recordModified(linePath, expLine, actLine);
            }
        }
    }

    private void compareLinesUnordered(String[] expected, String[] actual, ComparisonRules rules) {
        List<String> expList = new ArrayList<>(Arrays.asList(expected));
        List<String> actList = new ArrayList<>(Arrays.asList(actual));
        boolean[] matchedActual = new boolean[actList.size()];

        for (int i = 0; i < expList.size(); i++) {
            String expLine = expList.get(i);
            FieldPath linePath = FieldPath.of("line").index(i + 1);
            boolean found = false;

            for (int j = 0; j < actList.size(); j++) {
                if (!matchedActual[j] && rules.areValuesEqual(expLine, actList.get(j))) {
                    matchedActual[j] = true;
                    found = true;
                    recordMatch(linePath, expLine);
                    break;
                }
            }

            if (!found) {
                recordRemoved(linePath, expLine);
            }
        }

        for (int j = 0; j < actList.size(); j++) {
            if (!matchedActual[j]) {
                FieldPath linePath = FieldPath.of("line").index(j + 1);
                recordAdded(linePath, actList.get(j));
            }
        }
    }
}
