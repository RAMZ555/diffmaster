package com.diffmaster.core;

/**
 * Type of difference found during comparison.
 */
public enum DiffType {
    /** Both values match exactly */
    MATCH,
    /** Value exists in expected but not in actual */
    REMOVED,
    /** Value exists in actual but not in expected */
    ADDED,
    /** Value exists in both but differs */
    MODIFIED
}
