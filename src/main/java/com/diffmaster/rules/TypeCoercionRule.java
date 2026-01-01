package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Rule for type coercion during comparison.
 * "123" equals 123, "true" equals true
 */
public class TypeCoercionRule implements Rule {
    @Override
    public String getName() {
        return "TypeCoercion";
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
        if (expected.equals(actual))
            return true;

        // Try numeric comparison
        Double numExp = toDouble(expected);
        Double numAct = toDouble(actual);
        if (numExp != null && numAct != null) {
            return numExp.equals(numAct);
        }

        // Try boolean comparison
        Boolean boolExp = toBoolean(expected);
        Boolean boolAct = toBoolean(actual);
        if (boolExp != null && boolAct != null) {
            return boolExp.equals(boolAct);
        }

        // Fallback to string comparison
        return expected.toString().equals(actual.toString());
    }

    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        String str = value.toString().toLowerCase();
        if (str.equals("true") || str.equals("1") || str.equals("yes")) {
            return true;
        }
        if (str.equals("false") || str.equals("0") || str.equals("no")) {
            return false;
        }
        return null;
    }
}
