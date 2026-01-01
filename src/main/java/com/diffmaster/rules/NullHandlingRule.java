package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Rule to handle null values equivalence.
 * null = "" = "null" = "NULL"
 */
public class NullHandlingRule implements Rule {
    private final boolean nullEqualsEmpty;
    private final boolean nullEqualsNullString;

    public NullHandlingRule() {
        this(true, true);
    }

    public NullHandlingRule(boolean nullEqualsEmpty, boolean nullEqualsNullString) {
        this.nullEqualsEmpty = nullEqualsEmpty;
        this.nullEqualsNullString = nullEqualsNullString;
    }

    @Override
    public String getName() {
        return "NullHandling";
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
        Object normExp = normalize(expected);
        Object normAct = normalize(actual);
        if (normExp == null && normAct == null)
            return true;
        if (normExp == null || normAct == null)
            return false;
        return normExp.equals(normAct);
    }

    private Object normalize(Object value) {
        if (value == null)
            return null;
        String str = value.toString();
        if (nullEqualsEmpty && str.isEmpty()) {
            return null;
        }
        if (nullEqualsNullString && (str.equalsIgnoreCase("null"))) {
            return null;
        }
        return value;
    }
}
