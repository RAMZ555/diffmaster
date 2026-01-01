package com.diffmaster.report;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.exception.ReportGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Generates specific JSON output of the comparison result.
 * Ideal for API integration or downstream processing.
 */
public class JsonReportGenerator implements ReportGenerator {
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void generate(ComparisonResult result, String expectedName, String actualName, Path outputPath) {
        try {
            mapper.writeValue(outputPath.toFile(), result);
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to write JSON report", e);
        }
    }

    @Override
    public String generateString(ComparisonResult result, String expectedName, String actualName) {
        try {
            return mapper.writeValueAsString(result);
        } catch (IOException e) {
            throw new ReportGenerationException("Failed to generate JSON string", e);
        }
    }
}
