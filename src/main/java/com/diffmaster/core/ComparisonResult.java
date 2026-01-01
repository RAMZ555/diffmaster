package com.diffmaster.core;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Complete result of a comparison operation.
 */
@Getter
@Builder
@ToString
public class ComparisonResult {
    /** All differences found (including matches if requested) */
    @Builder.Default
    private final List<Difference> differences = new ArrayList<>();

    /** Summary statistics */
    private final ComparisonSummary summary;

    /** When comparison was performed */
    @Builder.Default
    private final Instant timestamp = Instant.now();

    /** Whether to include matching fields in differences list */
    @Builder.Default
    private final boolean includeMatches = false;

    /**
     * Check if expected and actual match completely.
     */
    public boolean isMatch() {
        return differences.stream().noneMatch(Difference::isDifferent);
    }

    /**
     * Check if there are any differences.
     */
    public boolean hasDifferences() {
        return !isMatch();
    }

    /**
     * Get only the actual differences (excluding matches).
     */
    public List<Difference> getOnlyDifferences() {
        return differences.stream()
                .filter(Difference::isDifferent)
                .collect(Collectors.toList());
    }

    /**
     * Get differences of a specific type.
     */
    public List<Difference> getDifferencesByType(DiffType type) {
        return differences.stream()
                .filter(d -> d.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get differences of a specific severity.
     */
    public List<Difference> getDifferencesBySeverity(DiffSeverity severity) {
        return differences.stream()
                .filter(d -> d.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * Get differences matching a path pattern.
     */
    public List<Difference> getDifferencesAtPath(String pathPattern) {
        return differences.stream()
                .filter(d -> d.getPath() != null && d.getPath().matches(pathPattern))
                .collect(Collectors.toList());
    }

    /**
     * Get count of actual differences.
     */
    public int getDifferenceCount() {
        return (int) differences.stream().filter(Difference::isDifferent).count();
    }

    /**
     * Print a simple summary to console.
     */
    public void printSummary() {
        System.out.println(summary.toDisplayString());
        if (hasDifferences()) {
            System.out.println("\nDifferences:");
            getOnlyDifferences()
                    .forEach(d -> System.out.printf("  [%s] %s: %s%n", d.getType(), d.getPathString(), d.getMessage()));
        }
    }

    /**
     * Create a result indicating a perfect match.
     */
    public static ComparisonResult perfectMatch(String expectedSource, String actualSource, int fieldCount,
            long durationMs) {
        return ComparisonResult.builder()
                .summary(ComparisonSummary.builder()
                        .totalFields(fieldCount)
                        .matchCount(fieldCount)
                        .modifiedCount(0)
                        .addedCount(0)
                        .removedCount(0)
                        .durationMs(durationMs)
                        .expectedSource(expectedSource)
                        .actualSource(actualSource)
                        .build())
                .build();
    }
}
