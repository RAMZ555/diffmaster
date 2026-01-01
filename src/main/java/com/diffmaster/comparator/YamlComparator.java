package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.rules.ComparisonRules;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * YAML comparison (converts to JSON tree internally).
 */
public class YamlComparator extends AbstractComparator {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private final JsonComparator jsonComparator = new JsonComparator();

    @Override
    public String getFormat() {
        return "yaml";
    }

    @Override
    public boolean supports(String format) {
        return "yaml".equalsIgnoreCase(format) || "yml".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        try {
            // Convert YAML to JSON for comparison
            JsonNode expNode = yamlMapper.readTree(expected);
            JsonNode actNode = yamlMapper.readTree(actual);

            String expJson = jsonMapper.writeValueAsString(expNode);
            String actJson = jsonMapper.writeValueAsString(actNode);

            ComparisonResult result = jsonComparator.compare(expJson, actJson, rules);

            // Update source names
            return ComparisonResult.builder()
                    .differences(result.getDifferences())
                    .summary(ComparisonSummary.builder()
                            .totalFields(result.getSummary().getTotalFields())
                            .matchCount(result.getSummary().getMatchCount())
                            .modifiedCount(result.getSummary().getModifiedCount())
                            .addedCount(result.getSummary().getAddedCount())
                            .removedCount(result.getSummary().getRemovedCount())
                            .durationMs(result.getSummary().getDurationMs())
                            .expectedSource("expected.yaml")
                            .actualSource("actual.yaml")
                            .format("yaml")
                            .build())
                    .build();
        } catch (Exception e) {
            throw new ComparisonException("Failed to parse YAML: " + e.getMessage(), e);
        }
    }
}
