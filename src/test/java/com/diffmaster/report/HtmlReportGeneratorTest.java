package com.diffmaster.report;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.core.ComparisonSummary;
import com.diffmaster.core.Difference;
import com.diffmaster.core.FieldPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HtmlReportGeneratorTest {

    @Test
    void testReportGenerationParams() {
        ComparisonResult result = ComparisonResult.builder()
                .summary(ComparisonSummary.builder()
                        .totalFields(10)
                        .matchCount(9)
                        .modifiedCount(1)
                        .build())
                .differences(List.of(Difference.modified(FieldPath.of("test"), "A", "B")))
                .build();

        HtmlReportGenerator generator = new HtmlReportGenerator("Test Report", true, false);
        String html = generator.generateString(result, "exp.json", "act.json");

        assertNotNull(html);
        assertTrue(html.contains("Test Report"));
        assertTrue(html.contains("dark")); // Dark mode class
        assertTrue(html.contains("exp.json"));
    }
}
