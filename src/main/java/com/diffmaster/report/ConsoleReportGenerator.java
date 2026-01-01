package com.diffmaster.report;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.core.ComparisonSummary;
import com.diffmaster.core.DiffType;
import com.diffmaster.core.Difference;
import com.diffmaster.exception.ReportGenerationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates a clean text-based report for console output or logs.
 */
public class ConsoleReportGenerator implements ReportGenerator {
    private final boolean useColors;

    // ANSI Color codes
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";

    public ConsoleReportGenerator() {
        this(true);
    }

    public ConsoleReportGenerator(boolean useColors) {
        this.useColors = useColors;
    }

    @Override
    public void generate(ComparisonResult result, String expectedName, String actualName, Path outputPath) {
        try {
            Files.writeString(outputPath, generateString(result, expectedName, actualName));
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to write console report to file", e);
        }
    }

    @Override
    public String generateString(ComparisonResult result, String expectedName, String actualName) {
        StringBuilder sb = new StringBuilder();
        ComparisonSummary summary = result.getSummary();

        // Header
        header(sb, "DIFFMASTER COMPARISON REPORT");
        sb.append("Expected: ").append(expectedName).append("\n");
        sb.append("Actual:   ").append(actualName).append("\n");
        sb.append("Time:     ").append(result.getTimestamp()).append("\n");
        sb.append("\n");

        // Summary
        header(sb, "SUMMARY");
        if (result.isMatch()) {
            sb.append(color(GREEN, "✓ Perfect Match!")).append("\n");
        } else {
            sb.append(color(RED, "✗ Differences Found")).append("\n");
        }
        sb.append("Total Fields: ").append(summary.getTotalFields()).append("\n");
        sb.append("Match Rate:   ").append(String.format("%.1f%%", summary.getMatchPercentage())).append("\n");
        sb.append("Differences:  ").append(summary.getDifferenceCount()).append(" (")
                .append(color(YELLOW, summary.getModifiedCount() + " modified")).append(", ")
                .append(color(GREEN, summary.getAddedCount() + " added")).append(", ")
                .append(color(RED, summary.getRemovedCount() + " removed")).append(")\n");
        sb.append("Duration:     ").append(summary.getDurationMs()).append("ms\n");
        sb.append("\n");

        // Differences
        if (result.hasDifferences()) {
            header(sb, "DIFFERENCES");
            List<Difference> diffs = result.getOnlyDifferences();

            // Group by type simply by listing them
            for (Difference diff : diffs) {
                appendDiff(sb, diff);
            }
        }

        return sb.toString();
    }

    private void appendDiff(StringBuilder sb, Difference diff) {
        String typeIcon = switch (diff.getType()) {
            case ADDED -> "+";
            case REMOVED -> "-";
            case MODIFIED -> "M";
            default -> " ";
        };

        String color = switch (diff.getType()) {
            case ADDED -> GREEN;
            case REMOVED -> RED;
            case MODIFIED -> YELLOW;
            default -> RESET;
        };

        sb.append(color(color, String.format("[%s] %s", typeIcon, diff.getPathString()))).append("\n");

        if (diff.getType() == DiffType.MODIFIED) {
            sb.append("    Expected: ").append(diff.getExpectedAsString()).append("\n");
            sb.append("    Actual:   ").append(diff.getActualAsString()).append("\n");
        } else if (diff.getType() == DiffType.ADDED) {
            sb.append("    Value:    ").append(diff.getActualAsString()).append("\n");
        } else if (diff.getType() == DiffType.REMOVED) {
            sb.append("    Value:    ").append(diff.getExpectedAsString()).append("\n");
        }
        sb.append("\n");
    }

    private void header(StringBuilder sb, String title) {
        sb.append("=== ").append(title).append(" ===\n");
    }

    private String color(String code, String text) {
        if (!useColors)
            return text;
        return code + text + RESET;
    }
}
