package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.input.DataSource;
import com.diffmaster.rules.ComparisonRules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for comparators with common functionality.
 */
public abstract class AbstractComparator implements DataComparator {
    protected final List<Difference> differences = new ArrayList<>();
    protected int fieldsCompared = 0;
    protected int matchCount = 0;
    protected int modifiedCount = 0;
    protected int addedCount = 0;
    protected int removedCount = 0;

    @Override
    public ComparisonResult compare(DataSource expected, DataSource actual, ComparisonRules rules) {
        try {
            return compare(expected.getContent(), actual.getContent(), rules);
        } catch (IOException e) {
            throw new ComparisonException("Failed to read data source", e);
        }
    }

    /**
     * Reset counters for new comparison.
     */
    protected void reset() {
        differences.clear();
        fieldsCompared = 0;
        matchCount = 0;
        modifiedCount = 0;
        addedCount = 0;
        removedCount = 0;
    }

    /**
     * Record a match.
     */
    protected void recordMatch(FieldPath path, Object value) {
        fieldsCompared++;
        matchCount++;
    }

    /**
     * Record a modification.
     */
    protected void recordModified(FieldPath path, Object expected, Object actual) {
        fieldsCompared++;
        modifiedCount++;
        differences.add(Difference.modified(path, expected, actual));
    }

    /**
     * Record an addition (in actual, not in expected).
     */
    protected void recordAdded(FieldPath path, Object value) {
        fieldsCompared++;
        addedCount++;
        differences.add(Difference.added(path, value));
    }

    /**
     * Record a removal (in expected, not in actual).
     */
    protected void recordRemoved(FieldPath path, Object value) {
        fieldsCompared++;
        removedCount++;
        differences.add(Difference.removed(path, value));
    }

    /**
     * Build the comparison result.
     */
    protected ComparisonResult buildResult(String expectedName, String actualName, long durationMs) {
        ComparisonSummary summary = ComparisonSummary.builder()
                .totalFields(fieldsCompared)
                .matchCount(matchCount)
                .modifiedCount(modifiedCount)
                .addedCount(addedCount)
                .removedCount(removedCount)
                .durationMs(durationMs)
                .expectedSource(expectedName)
                .actualSource(actualName)
                .format(getFormat())
                .build();

        return ComparisonResult.builder()
                .differences(new ArrayList<>(differences))
                .summary(summary)
                .build();
    }
}
