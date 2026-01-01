package com.diffmaster.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a single difference found during comparison.
 */
@Getter
@Builder
@ToString
public class Difference {
    /** Path to the differing field */
    private final FieldPath path;

    /** Type of difference */
    private final DiffType type;

    /** Severity of the difference */
    @Builder.Default
    private final DiffSeverity severity = DiffSeverity.ERROR;

    /** Expected value (null if ADDED) */
    private final Object expectedValue;

    /** Actual value (null if REMOVED) */
    private final Object actualValue;

    /** Human-readable description */
    private final String message;

    /** Line number in source (if applicable) */
    private final Integer lineNumber;

    /** Column number in source (if applicable) */
    private final Integer columnNumber;

    /**
     * Create a MATCH difference.
     */
    public static Difference match(FieldPath path, Object value) {
        return Difference.builder()
                .path(path)
                .type(DiffType.MATCH)
                .severity(DiffSeverity.INFO)
                .expectedValue(value)
                .actualValue(value)
                .message("Values match")
                .build();
    }

    /**
     * Create a MODIFIED difference.
     */
    public static Difference modified(FieldPath path, Object expected, Object actual) {
        return Difference.builder()
                .path(path)
                .type(DiffType.MODIFIED)
                .severity(DiffSeverity.ERROR)
                .expectedValue(expected)
                .actualValue(actual)
                .message(String.format("Value changed from '%s' to '%s'", expected, actual))
                .build();
    }

    /**
     * Create an ADDED difference.
     */
    public static Difference added(FieldPath path, Object value) {
        return Difference.builder()
                .path(path)
                .type(DiffType.ADDED)
                .severity(DiffSeverity.WARNING)
                .actualValue(value)
                .message(String.format("New value added: '%s'", value))
                .build();
    }

    /**
     * Create a REMOVED difference.
     */
    public static Difference removed(FieldPath path, Object value) {
        return Difference.builder()
                .path(path)
                .type(DiffType.REMOVED)
                .severity(DiffSeverity.ERROR)
                .expectedValue(value)
                .message(String.format("Value removed: '%s'", value))
                .build();
    }

    /**
     * Check if this represents a match.
     */
    public boolean isMatch() {
        return type == DiffType.MATCH;
    }

    /**
     * Check if this represents any kind of difference.
     */
    public boolean isDifferent() {
        return type != DiffType.MATCH;
    }

    /**
     * Get a formatted path string.
     */
    public String getPathString() {
        return path != null ? path.toString() : "";
    }

    /**
     * Get expected value as string.
     */
    public String getExpectedAsString() {
        return expectedValue != null ? expectedValue.toString() : "null";
    }

    /**
     * Get actual value as string.
     */
    public String getActualAsString() {
        return actualValue != null ? actualValue.toString() : "null";
    }
}
