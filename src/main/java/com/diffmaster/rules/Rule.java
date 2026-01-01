package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;

/**
 * Base interface for all comparison rules.
 */
public interface Rule {
    /**
     * Get the name of this rule.
     */
    String getName();

    /**
     * Check if this rule applies to the given path.
     */
    boolean appliesTo(FieldPath path);

    /**
     * Apply the rule and determine if differences should be ignored.
     * Returns true if the difference should be ignored.
     */
    boolean shouldIgnore(FieldPath path, Object expected, Object actual);

    /**
     * Check if the rule considers two values equal.
     * Returns true if values should be treated as equal.
     */
    default boolean areEqual(Object expected, Object actual) {
        return false; // Override for tolerance-based rules
    }
}
