package com.diffmaster.report;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.core.ComparisonSummary;
import com.diffmaster.core.Difference;
import com.diffmaster.exception.ReportGenerationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates a Markdown report suitable for GitHub Comments or documentation.
 */
public class MarkdownReportGenerator implements ReportGenerator {

    @Override
    public void generate(ComparisonResult result, String expectedName, String actualName, Path outputPath) {
        try {
            Files.writeString(outputPath, generateString(result, expectedName, actualName));
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to write Markdown report", e);
        }
    }

    @Override
    public String generateString(ComparisonResult result, String expectedName, String actualName) {
        StringBuilder sb = new StringBuilder();
        ComparisonSummary summary = result.getSummary();

        // Title
        sb.append("# DiffMaster Comparison Report\n\n");

        // Status Badge
        if (result.isMatch()) {
            sb.append("![Match](https://img.shields.io/badge/Status-Match-brightgreen)\n");
        } else {
            sb.append("![Diff](https://img.shields.io/badge/Status-Differences-red)\n");
        }
        sb.append("\n");

        // Metadata Table
        sb.append("| Metric | Value |\n");
        sb.append("|---|---|\n");
        sb.append(String.format("| Expected | `%s` |\n", expectedName));
        sb.append(String.format("| Actual | `%s` |\n", actualName));
        sb.append(String.format("| Match Rate | %.1f%% |\n", summary.getMatchPercentage()));
        sb.append(String.format("| Total Fields | %d |\n", summary.getTotalFields()));
        sb.append("\n");

        // Summary Statistics
        if (result.hasDifferences()) {
            sb.append("## Differences Summary\n");
            sb.append("- **Modified**: ").append(summary.getModifiedCount()).append("\n");
            sb.append("- **Added**: ").append(summary.getAddedCount()).append("\n");
            sb.append("- **Removed**: ").append(summary.getRemovedCount()).append("\n");
            sb.append("\n");
        }

        // Detailed Differences
        if (result.hasDifferences()) {
            sb.append("## Detailed Differences\n\n");
            sb.append("| Type | Path | Expected | Actual |\n");
            sb.append("|---|---|---|---|\n");

            List<Difference> diffs = result.getOnlyDifferences();
            int limit = 100; // Cap to avoid huge MD files
            for (int i = 0; i < Math.min(diffs.size(), limit); i++) {
                Difference diff = diffs.get(i);
                sb.append(String.format("| %s | `%s` | %s | %s |\n",
                        getIcon(diff),
                        diff.getPathString(),
                        formatValue(diff.getExpectedAsString()),
                        formatValue(diff.getActualAsString())));
            }

            if (diffs.size() > limit) {
                sb.append(String.format("\n*...and %d more differences omitted.*", diffs.size() - limit));
            }
        } else {
            sb.append("## ✨ No Differences Found\n");
            sb.append("Both sources are identical according to the configured rules.");
        }

        return sb.toString();
    }

    private String getIcon(Difference diff) {
        return switch (diff.getType()) {
            case MODIFIED -> "⚠️ MODIFIED";
            case ADDED -> "➕ ADDED";
            case REMOVED -> "➖ REMOVED";
            default -> "MATCH";
        };
    }

    private String formatValue(String value) {
        if (value == null || value.equals("null"))
            return "*null*";
        if (value.isEmpty())
            return "*empty*";
        // Escape pipes for markdown table
        String escaped = value.replace("|", "\\|").replace("\n", " ");
        if (escaped.length() > 50) {
            return "`" + escaped.substring(0, 47) + "...`";
        }
        return "`" + escaped + "`";
    }
}
