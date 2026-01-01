package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import lombok.Getter;

/**
 * Rule for numeric comparison with tolerance.
 * Example: 1.001 ≈ 1.000 with tolerance 0.01
 */
@Getter
public class NumericToleranceRule implements Rule {
    private final double tolerance;

    public NumericToleranceRule(double tolerance) {
        this.tolerance = Math.abs(tolerance);
    }

    @Override
    public String getName() {
        return "NumericTolerance";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        return true; // Applied at value level, check in areEqual
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return areEqual(expected, actual);
    }

    @Override
    public boolean areEqual(Object expected, Object actual) {
        Double exp = toDouble(expected);
        Double act = toDouble(actual);
        if (exp == null || act == null) {
            return false; // Not numeric
        }
        return Math.abs(exp - act) <= tolerance;
    }

    private Double toDouble(Object value) {
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
