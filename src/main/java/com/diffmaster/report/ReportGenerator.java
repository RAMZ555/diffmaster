package com.diffmaster.report;

import com.diffmaster.core.ComparisonResult;

import java.nio.file.Path;

/**
 * Base interface for report generators.
 */
public interface ReportGenerator {
    /**
     * Generate report and save to file.
     */
    void generate(ComparisonResult result, String expectedName, String actualName, Path outputPath);

    /**
     * Generate report and return as string.
     */
    String generateString(ComparisonResult result, String expectedName, String actualName);
}
