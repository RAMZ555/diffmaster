package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for all comparison rules.
 */
@Getter
public class ComparisonRules {
    private final List<Rule> rules = new ArrayList<>();

    private boolean ignoreArrayOrder = false;
    private boolean caseInsensitive = false;
    private boolean ignoreWhitespace = false;
    private boolean nullEqualsEmpty = false;
    private boolean typeCoercion = false;
    private Double numericTolerance = null;
    private Duration dateTolerance = null;

    /**
     * Add a custom rule.
     */
    public ComparisonRules addRule(Rule rule) {
        rules.add(rule);
        // Track special flags
        if (rule instanceof ArrayOrderRule) {
            ignoreArrayOrder = ((ArrayOrderRule) rule).isIgnoreOrder();
        } else if (rule instanceof CaseInsensitiveRule) {
            caseInsensitive = true;
        } else if (rule instanceof WhitespaceRule) {
            ignoreWhitespace = true;
        } else if (rule instanceof NullHandlingRule) {
            nullEqualsEmpty = true;
        } else if (rule instanceof TypeCoercionRule) {
            typeCoercion = true;
        } else if (rule instanceof NumericToleranceRule) {
            numericTolerance = ((NumericToleranceRule) rule).getTolerance();
        } else if (rule instanceof DateToleranceRule) {
            dateTolerance = ((DateToleranceRule) rule).getTolerance();
        }
        return this;
    }

    /**
     * Check if a path should be ignored.
     */
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        for (Rule rule : rules) {
            if (rule instanceof IgnoreFieldRule || rule instanceof IgnorePatternRule) {
                if (rule.shouldIgnore(path, expected, actual)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if two values are considered equal under current rules.
     */
    public boolean areValuesEqual(Object expected, Object actual) {
        if (expected == null && actual == null)
            return true;
        if (expected == null || actual == null) {
            // Check null handling rule
            if (nullEqualsEmpty) {
                NullHandlingRule rule = new NullHandlingRule();
                if (rule.areEqual(expected, actual))
                    return true;
            }
            return false;
        }

        // Direct equality
        if (expected.equals(actual))
            return true;

        // Try numeric tolerance
        if (numericTolerance != null) {
            NumericToleranceRule rule = new NumericToleranceRule(numericTolerance);
            if (rule.areEqual(expected, actual))
                return true;
        }

        // Try date tolerance
        if (dateTolerance != null) {
            DateToleranceRule rule = new DateToleranceRule(dateTolerance);
            if (rule.areEqual(expected, actual))
                return true;
        }

        // Try case insensitive
        if (caseInsensitive) {
            CaseInsensitiveRule rule = new CaseInsensitiveRule();
            if (rule.areEqual(expected, actual))
                return true;
        }

        // Try whitespace normalization
        if (ignoreWhitespace) {
            WhitespaceRule rule = new WhitespaceRule();
            if (rule.areEqual(expected, actual))
                return true;
        }

        // Try type coercion
        if (typeCoercion) {
            TypeCoercionRule rule = new TypeCoercionRule();
            if (rule.areEqual(expected, actual))
                return true;
        }

        return false;
    }

    /**
     * Create empty rules.
     */
    public static ComparisonRules empty() {
        return new ComparisonRules();
    }

    /**
     * Create default lenient rules.
     */
    public static ComparisonRules lenient() {
        return new ComparisonRules()
                .addRule(new WhitespaceRule())
                .addRule(new CaseInsensitiveRule())
                .addRule(new NullHandlingRule())
                .addRule(new TypeCoercionRule());
    }

    /**
     * Create strict rules (no tolerance).
     */
    public static ComparisonRules strict() {
        return new ComparisonRules();
    }
}
