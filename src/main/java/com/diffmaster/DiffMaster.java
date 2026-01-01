package com.diffmaster;

import com.diffmaster.comparator.ComparatorFactory;
import com.diffmaster.comparator.DataComparator;
import com.diffmaster.core.ComparisonResult;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.input.*;
import com.diffmaster.report.HtmlReportGenerator;
import com.diffmaster.rules.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * DiffMaster - Universal Data Comparator Library.
 *
 * <p>
 * Main entry point for comparing data files. Use the fluent API:
 *
 * <pre>{@code
 * // Simple comparison
 * boolean match = DiffMaster.compare(expected, actual).isMatch();
 *
 * // With rules
 * ComparisonResult result = DiffMaster
 *         .compare(expectedFile, actualFile)
 *         .ignoreFields("id", "timestamp")
 *         .ignoreArrayOrder()
 *         .execute();
 *
 * // Generate HTML report
 * DiffMaster
 *         .compare(expected, actual)
 *         .ignoreFields("id")
 *         .asHtml()
 *         .withTitle("API Comparison")
 *         .saveTo("report.html");
 * }</pre>
 */
public class DiffMaster {

    private DiffMaster() {
        // Utility class
    }

    // ========== STATIC ENTRY POINTS ==========

    /**
     * Compare two files.
     */
    public static DiffMasterBuilder compare(Path expected, Path actual) {
        return new DiffMasterBuilder(new FileSource(expected), new FileSource(actual));
    }

    /**
     * Compare two files.
     */
    public static DiffMasterBuilder compare(File expected, File actual) {
        return new DiffMasterBuilder(new FileSource(expected), new FileSource(actual));
    }

    /**
     * Compare two file paths as strings.
     */
    public static DiffMasterBuilder compare(String expectedPath, String actualPath) {
        // Check if paths are files or raw content
        try {
            Path expPath = Path.of(expectedPath);
            Path actPath = Path.of(actualPath);

            if (Files.exists(expPath) && Files.exists(actPath)) {
                return new DiffMasterBuilder(new FileSource(expPath), new FileSource(actPath));
            }
        } catch (Exception e) {
            // Not a valid file path (e.g. contains special chars), treat as raw content
        }

        // Treat as raw content
        return new DiffMasterBuilder(new StringSource(expectedPath), new StringSource(actualPath));
    }

    /**
     * Compare two strings of content.
     */
    public static DiffMasterBuilder compareStrings(String expected, String actual) {
        return new DiffMasterBuilder(new StringSource(expected), new StringSource(actual));
    }

    /**
     * Compare two URLs.
     */
    public static DiffMasterBuilder compare(URL expected, URL actual) {
        return new DiffMasterBuilder(new UrlSource(expected), new UrlSource(actual));
    }

    /**
     * Compare two input streams.
     */
    public static DiffMasterBuilder compare(InputStream expected, InputStream actual) {
        try {
            return new DiffMasterBuilder(
                    new StreamSource(expected, null),
                    new StreamSource(actual, null));
        } catch (IOException e) {
            throw new ComparisonException("Failed to read input streams", e);
        }
    }

    /**
     * Compare two data sources.
     */
    public static DiffMasterBuilder compare(DataSource expected, DataSource actual) {
        return new DiffMasterBuilder(expected, actual);
    }

    /**
     * Quick check if two JSON strings match.
     */
    public static boolean jsonEquals(String expected, String actual) {
        return compareStrings(expected, actual).asJson().execute().isMatch();
    }

    /**
     * Quick check if two XML strings match.
     */
    public static boolean xmlEquals(String expected, String actual) {
        return compareStrings(expected, actual).asXml().execute().isMatch();
    }

    /**
     * Quick check if two files match.
     */
    public static boolean filesEqual(Path expected, Path actual) {
        return compare(expected, actual).execute().isMatch();
    }

    // ========== BUILDER CLASS ==========

    /**
     * Fluent builder for configuring comparison.
     */
    public static class DiffMasterBuilder {
        private final DataSource expected;
        private final DataSource actual;
        private final ComparisonRules rules = new ComparisonRules();
        private String format = null;

        DiffMasterBuilder(DataSource expected, DataSource actual) {
            this.expected = expected;
            this.actual = actual;
        }

        // ========== FORMAT SPECIFICATION ==========

        /**
         * Force JSON format.
         */
        public DiffMasterBuilder asJson() {
            this.format = "json";
            return this;
        }

        /**
         * Force XML format.
         */
        public DiffMasterBuilder asXml() {
            this.format = "xml";
            return this;
        }

        /**
         * Force CSV format.
         */
        public DiffMasterBuilder asCsv() {
            this.format = "csv";
            return this;
        }

        /**
         * Force YAML format.
         */
        public DiffMasterBuilder asYaml() {
            this.format = "yaml";
            return this;
        }

        /**
         * Force Excel format.
         */
        public DiffMasterBuilder asExcel() {
            this.format = "excel";
            return this;
        }

        /**
         * Force text format.
         */
        public DiffMasterBuilder asText() {
            this.format = "text";
            return this;
        }

        /**
         * Force properties format.
         */
        public DiffMasterBuilder asProperties() {
            this.format = "properties";
            return this;
        }

        // ========== IGNORE RULES ==========

        /**
         * Ignore specific fields by name.
         */
        public DiffMasterBuilder ignoreFields(String... fields) {
            rules.addRule(new IgnoreFieldRule(fields));
            return this;
        }

        /**
         * Ignore fields matching regex pattern.
         */
        public DiffMasterBuilder ignorePattern(String... patterns) {
            rules.addRule(new IgnorePatternRule(patterns));
            return this;
        }

        /**
         * Ignore fields ending with specified suffixes.
         */
        public DiffMasterBuilder ignoreFieldsEndingWith(String... suffixes) {
            for (String suffix : suffixes) {
                rules.addRule(new IgnorePatternRule(".*" + suffix + "$"));
            }
            return this;
        }

        // ========== ARRAY HANDLING ==========

        /**
         * Ignore array element order. [A, B] equals [B, A].
         */
        public DiffMasterBuilder ignoreArrayOrder() {
            rules.addRule(new ArrayOrderRule(true));
            return this;
        }

        /**
         * Consider array order in comparison (default).
         */
        public DiffMasterBuilder strictArrayOrder() {
            rules.addRule(new ArrayOrderRule(false));
            return this;
        }

        // ========== TOLERANCE RULES ==========

        /**
         * Allow numeric difference within tolerance.
         * Example: numericTolerance(0.01) makes 1.001 equal to 1.000
         */
        public DiffMasterBuilder numericTolerance(double tolerance) {
            rules.addRule(new NumericToleranceRule(tolerance));
            return this;
        }

        /**
         * Allow date/time difference within tolerance.
         */
        public DiffMasterBuilder dateTolerance(Duration tolerance) {
            rules.addRule(new DateToleranceRule(tolerance));
            return this;
        }

        /**
         * Allow date/time difference in seconds.
         */
        public DiffMasterBuilder dateToleranceSeconds(long seconds) {
            return dateTolerance(Duration.ofSeconds(seconds));
        }

        /**
         * Allow date/time difference in minutes.
         */
        public DiffMasterBuilder dateToleranceMinutes(long minutes) {
            return dateTolerance(Duration.ofMinutes(minutes));
        }

        // ========== STRING HANDLING ==========

        /**
         * Ignore case in string comparison. "ABC" equals "abc".
         */
        public DiffMasterBuilder caseInsensitive() {
            rules.addRule(new CaseInsensitiveRule());
            return this;
        }

        /**
         * Normalize whitespace: trim and collapse multiple spaces.
         */
        public DiffMasterBuilder ignoreWhitespace() {
            rules.addRule(new WhitespaceRule());
            return this;
        }

        /**
         * Ignore all whitespace completely.
         */
        public DiffMasterBuilder ignoreAllWhitespace() {
            rules.addRule(WhitespaceRule.ignoreAll());
            return this;
        }

        // ========== NULL HANDLING ==========

        /**
         * Treat null, empty string, and "null" as equal.
         */
        public DiffMasterBuilder nullEqualsEmpty() {
            rules.addRule(new NullHandlingRule());
            return this;
        }

        // ========== TYPE COERCION ==========

        /**
         * Allow type coercion: "123" equals 123, "true" equals true.
         */
        public DiffMasterBuilder typeCoercion() {
            rules.addRule(new TypeCoercionRule());
            return this;
        }

        // ========== PRESET CONFIGURATIONS ==========

        /**
         * Apply lenient comparison (whitespace, case, null handling, type coercion).
         */
        public DiffMasterBuilder lenient() {
            return ignoreWhitespace()
                    .caseInsensitive()
                    .nullEqualsEmpty()
                    .typeCoercion();
        }

        /**
         * Apply strict comparison (default).
         */
        public DiffMasterBuilder strict() {
            // Default - no extra rules
            return this;
        }

        // ========== CUSTOM RULES ==========

        /**
         * Add a custom rule.
         */
        public DiffMasterBuilder withRule(Rule rule) {
            rules.addRule(rule);
            return this;
        }

        // ========== EXECUTION ==========

        /**
         * Execute comparison and return result.
         */
        public ComparisonResult execute() {
            DataComparator comparator = getComparator();
            return comparator.compare(expected, actual, rules);
        }

        /**
         * Check if expected and actual match.
         */
        public boolean isMatch() {
            return execute().isMatch();
        }

        /**
         * Check if there are differences.
         */
        public boolean hasDifferences() {
            return execute().hasDifferences();
        }

        /**
         * Get the count of differences.
         */
        public int getDifferenceCount() {
            return execute().getDifferenceCount();
        }

        // ========== REPORTING ==========

        /**
         * Set up HTML report generation.
         */
        public HtmlReportBuilder asHtml() {
            return new HtmlReportBuilder(this);
        }

        /**
         * Print comparison summary to console.
         */
        public void printSummary() {
            execute().printSummary();
        }

        // ========== INTERNAL ==========

        private DataComparator getComparator() {
            if (format != null) {
                return ComparatorFactory.getComparator(format);
            }
            return ComparatorFactory.autoDetect(expected, actual);
        }

        ComparisonResult executeInternal() {
            return execute();
        }

        String getExpectedName() {
            return expected.getName();
        }

        String getActualName() {
            return actual.getName();
        }
    }

    // ========== HTML REPORT BUILDER ==========

    /**
     * Builder for HTML report configuration.
     */
    public static class HtmlReportBuilder {
        private final DiffMasterBuilder parent;
        private String title = "DiffMaster Comparison Report";
        private boolean darkMode = false;
        private boolean showMatchedFields = false;

        HtmlReportBuilder(DiffMasterBuilder parent) {
            this.parent = parent;
        }

        /**
         * Set report title.
         */
        public HtmlReportBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Enable dark mode.
         */
        public HtmlReportBuilder withDarkMode() {
            this.darkMode = true;
            return this;
        }

        /**
         * Enable dark mode conditionally.
         */
        public HtmlReportBuilder withDarkMode(boolean enabled) {
            this.darkMode = enabled;
            return this;
        }

        /**
         * Show matched fields in report (default: only show differences).
         */
        public HtmlReportBuilder showAllFields() {
            this.showMatchedFields = true;
            return this;
        }

        /**
         * Generate HTML report and save to file.
         */
        public void saveTo(String path) {
            saveTo(Path.of(path));
        }

        /**
         * Generate HTML report and save to file.
         */
        public void saveTo(Path path) {
            ComparisonResult result = parent.executeInternal();
            HtmlReportGenerator generator = new HtmlReportGenerator(title, darkMode, showMatchedFields);
            generator.generate(result, parent.getExpectedName(), parent.getActualName(), path);
        }

        /**
         * Generate HTML report and return as string.
         */
        public String generate() {
            ComparisonResult result = parent.executeInternal();
            HtmlReportGenerator generator = new HtmlReportGenerator(title, darkMode, showMatchedFields);
            return generator.generateString(result, parent.getExpectedName(), parent.getActualName());
        }
    }
}
