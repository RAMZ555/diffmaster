package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Rule for case-insensitive string comparison.
 * "ABC" equals "abc"
 */
public class CaseInsensitiveRule implements Rule {
    @Override
    public String getName() {
        return "CaseInsensitive";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        return true;
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return areEqual(expected, actual);
    }

    @Override
    public boolean areEqual(Object expected, Object actual) {
        if (expected == null && actual == null)
            return true;
        if (expected == null || actual == null)
            return false;
        String exp = expected.toString();
        String act = actual.toString();
        return exp.equalsIgnoreCase(act);
    }
}
