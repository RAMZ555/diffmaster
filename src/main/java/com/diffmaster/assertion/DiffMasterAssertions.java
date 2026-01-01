package com.diffmaster.assertion;

import com.diffmaster.DiffMaster;
import com.diffmaster.core.ComparisonResult;
import com.diffmaster.input.DataSource;
import com.diffmaster.input.FileSource;
import com.diffmaster.input.StringSource;
import com.diffmaster.rules.*;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Fluent Assertions for JUnit 5 / TestNG.
 */
public class DiffMasterAssertions {

    public static DiffMasterAssert assertThat(String actual) {
        return new DiffMasterAssert(new StringSource(actual, "actual", null));
    }

    public static DiffMasterAssert assertThat(File actual) {
        return new DiffMasterAssert(new FileSource(actual));
    }

    public static DiffMasterAssert assertThat(Path actual) {
        return new DiffMasterAssert(new FileSource(actual));
    }

    public static DiffMasterAssert assertThat(DataSource actual) {
        return new DiffMasterAssert(actual);
    }

    /**
     * Fluent assertion builder.
     */
    public static class DiffMasterAssert {
        private final DataSource actual;
        private final ComparisonRules rules = new ComparisonRules();

        private DiffMasterAssert(DataSource actual) {
            this.actual = actual;
        }

        public DiffMasterAssert ignoreFields(String... fields) {
            rules.addRule(new IgnoreFieldRule(fields));
            return this;
        }

        public DiffMasterAssert ignorePattern(String... patterns) {
            rules.addRule(new IgnorePatternRule(patterns));
            return this;
        }

        public DiffMasterAssert ignoreArrayOrder() {
            rules.addRule(new ArrayOrderRule(true));
            return this;
        }

        public DiffMasterAssert numericTolerance(double tolerance) {
            rules.addRule(new NumericToleranceRule(tolerance));
            return this;
        }

        public DiffMasterAssert dateTolerance(Duration tolerance) {
            rules.addRule(new DateToleranceRule(tolerance));
            return this;
        }

        public DiffMasterAssert lenient() {
            rules.addRule(new WhitespaceRule())
                    .addRule(new CaseInsensitiveRule())
                    .addRule(new NullHandlingRule())
                    .addRule(new TypeCoercionRule());
            return this;
        }

        public DiffMasterAssert withRule(Rule rule) {
            rules.addRule(rule);
            return this;
        }

        public void isEqualTo(String expected) {
            evaluate(new StringSource(expected, "expected", null));
        }

        public void isEqualTo(File expected) {
            evaluate(new FileSource(expected));
        }

        public void isEqualTo(Path expected) {
            evaluate(new FileSource(expected));
        }

        public void isEqualTo(DataSource expected) {
            evaluate(expected);
        }

        private void evaluate(DataSource expected) {
            // Manually build DiffMaster to inject our rules
            DiffMaster.DiffMasterBuilder builder = DiffMaster.compare(expected, actual);

            // Inject rules
            for (Rule rule : rules.getRules()) {
                builder.withRule(rule);
            }

            ComparisonResult result = builder.execute();

            if (!result.isMatch()) {
                StringBuilder msg = new StringBuilder();
                msg.append("Expected and actual are different:\n");

                result.getOnlyDifferences().forEach(diff -> {
                    msg.append(String.format("- [%s] %s: %s\n",
                            diff.getType(), diff.getPathString(), diff.getMessage()));
                });

                msg.append("\n");
                msg.append("Expected: ").append(expected.getContentSafe()).append("\n");
                msg.append("Actual:   ").append(actual.getContentSafe()).append("\n");

                throw new AssertionError(msg.toString());
            }
        }
    }
}
