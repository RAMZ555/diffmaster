package com.diffmaster.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Summary statistics of a comparison.
 */
@Getter
@Builder
@ToString
public class ComparisonSummary {
    /** Total fields compared */
    private final int totalFields;

    /** Number of matching fields */
    private final int matchCount;

    /** Number of modified fields */
    private final int modifiedCount;

    /** Number of added fields (in actual but not expected) */
    private final int addedCount;

    /** Number of removed fields (in expected but not actual) */
    private final int removedCount;

    /** Comparison duration in milliseconds */
    private final long durationMs;

    /** Expected source name/path */
    private final String expectedSource;

    /** Actual source name/path */
    private final String actualSource;

    /** File format compared */
    private final String format;

    /**
     * Get total number of differences.
     */
    public int getDifferenceCount() {
        return modifiedCount + addedCount + removedCount;
    }

    /**
     * Get match percentage.
     */
    public double getMatchPercentage() {
        if (totalFields == 0)
            return 100.0;
        return (matchCount * 100.0) / totalFields;
    }

    /**
     * Check if comparison found no differences.
     */
    public boolean isFullMatch() {
        return getDifferenceCount() == 0;
    }

    /**
     * Get a human-readable summary.
     */
    public String toDisplayString() {
        if (isFullMatch()) {
            return String.format("✓ Perfect match! %d fields compared in %dms", totalFields, durationMs);
        }
        return String.format("✗ Found %d differences: %d modified, %d added, %d removed (%.1f%% match)",
                getDifferenceCount(), modifiedCount, addedCount, removedCount, getMatchPercentage());
    }
}
