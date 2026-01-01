package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Rule to ignore array element order.
 * [A, B, C] equals [C, B, A]
 */
public class ArrayOrderRule implements Rule {
    private final boolean ignoreOrder;

    public ArrayOrderRule() {
        this(true);
    }

    public ArrayOrderRule(boolean ignoreOrder) {
        this.ignoreOrder = ignoreOrder;
    }

    @Override
    public String getName() {
        return "ArrayOrder";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        return true;
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return false; // This rule is handled specially by comparators
    }

    public boolean isIgnoreOrder() {
        return ignoreOrder;
    }
}
