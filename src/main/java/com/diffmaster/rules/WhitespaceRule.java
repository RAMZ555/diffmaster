package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Rule to normalize whitespace during comparison.
 * Trims and collapses multiple spaces to single space.
 */
public class WhitespaceRule implements Rule {
    private final boolean trim;
    private final boolean normalizeSpaces;
    private final boolean ignoreAllWhitespace;

    public WhitespaceRule() {
        this(true, true, false);
    }

    public WhitespaceRule(boolean trim, boolean normalizeSpaces, boolean ignoreAllWhitespace) {
        this.trim = trim;
        this.normalizeSpaces = normalizeSpaces;
        this.ignoreAllWhitespace = ignoreAllWhitespace;
    }

    public static WhitespaceRule ignoreAll() {
        return new WhitespaceRule(true, true, true);
    }

    @Override
    public String getName() {
        return "Whitespace";
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
        String exp = normalize(expected.toString());
        String act = normalize(actual.toString());
        return exp.equals(act);
    }

    public String normalize(String value) {
        if (value == null)
            return null;
        String result = value;
        if (ignoreAllWhitespace) {
            result = result.replaceAll("\\s+", "");
        } else {
            if (trim) {
                result = result.trim();
            }
            if (normalizeSpaces) {
                result = result.replaceAll("\\s+", " ");
            }
        }
        return result;
    }
}
