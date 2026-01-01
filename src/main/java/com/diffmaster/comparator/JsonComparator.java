package com.diffmaster.comparator;

import com.diffmaster.core.*;
import com.diffmaster.exception.ComparisonException;
import com.diffmaster.rules.ComparisonRules;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

/**
 * JSON deep comparison with full path tracking.
 */
public class JsonComparator extends AbstractComparator {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFormat() {
        return "json";
    }

    @Override
    public boolean supports(String format) {
        return "json".equalsIgnoreCase(format);
    }

    @Override
    public ComparisonResult compare(String expected, String actual, ComparisonRules rules) {
        reset();
        long startTime = System.currentTimeMillis();

        try {
            JsonNode expectedNode = mapper.readTree(expected);
            JsonNode actualNode = mapper.readTree(actual);

            compareNodes(expectedNode, actualNode, FieldPath.root(), rules);

            long duration = System.currentTimeMillis() - startTime;
            return buildResult("expected", "actual", duration);
        } catch (JsonProcessingException e) {
            throw new ComparisonException("Invalid JSON: " + e.getMessage(), e);
        }
    }

    private void compareNodes(JsonNode expected, JsonNode actual, FieldPath path, ComparisonRules rules) {
        // Check if path should be ignored
        if (rules.shouldIgnore(path, expected, actual)) {
            return;
        }

        // Handle nulls
        if (expected == null || expected.isNull()) {
            if (actual == null || actual.isNull()) {
                recordMatch(path, null);
            } else {
                recordAdded(path, nodeToValue(actual));
            }
            return;
        }

        if (actual == null || actual.isNull()) {
            recordRemoved(path, nodeToValue(expected));
            return;
        }

        // Compare by type
        if (expected.isObject() && actual.isObject()) {
            compareObjects((ObjectNode) expected, (ObjectNode) actual, path, rules);
        } else if (expected.isArray() && actual.isArray()) {
            compareArrays((ArrayNode) expected, (ArrayNode) actual, path, rules);
        } else {
            compareValues(expected, actual, path, rules);
        }
    }

    private void compareObjects(ObjectNode expected, ObjectNode actual, FieldPath path, ComparisonRules rules) {
        Set<String> allFields = new HashSet<>();
        expected.fieldNames().forEachRemaining(allFields::add);
        actual.fieldNames().forEachRemaining(allFields::add);

        for (String field : allFields) {
            FieldPath fieldPath = path.child(field);

            if (rules.shouldIgnore(fieldPath, null, null)) {
                continue;
            }

            JsonNode expectedValue = expected.get(field);
            JsonNode actualValue = actual.get(field);

            if (expectedValue == null) {
                recordAdded(fieldPath, nodeToValue(actualValue));
            } else if (actualValue == null) {
                recordRemoved(fieldPath, nodeToValue(expectedValue));
            } else {
                compareNodes(expectedValue, actualValue, fieldPath, rules);
            }
        }
    }

    private void compareArrays(ArrayNode expected, ArrayNode actual, FieldPath path, ComparisonRules rules) {
        if (rules.isIgnoreArrayOrder()) {
            compareArraysUnordered(expected, actual, path, rules);
        } else {
            compareArraysOrdered(expected, actual, path, rules);
        }
    }

    private void compareArraysOrdered(ArrayNode expected, ArrayNode actual, FieldPath path, ComparisonRules rules) {
        int maxLen = Math.max(expected.size(), actual.size());
        for (int i = 0; i < maxLen; i++) {
            FieldPath indexPath = path.index(i);
            if (i >= expected.size()) {
                recordAdded(indexPath, nodeToValue(actual.get(i)));
            } else if (i >= actual.size()) {
                recordRemoved(indexPath, nodeToValue(expected.get(i)));
            } else {
                compareNodes(expected.get(i), actual.get(i), indexPath, rules);
            }
        }
    }

    private void compareArraysUnordered(ArrayNode expected, ArrayNode actual, FieldPath path, ComparisonRules rules) {
        List<JsonNode> expectedList = new ArrayList<>();
        List<JsonNode> actualList = new ArrayList<>();
        expected.forEach(expectedList::add);
        actual.forEach(actualList::add);

        boolean[] matchedActual = new boolean[actualList.size()];

        for (int i = 0; i < expectedList.size(); i++) {
            JsonNode expItem = expectedList.get(i);
            boolean found = false;

            for (int j = 0; j < actualList.size(); j++) {
                if (!matchedActual[j] && nodesEqual(expItem, actualList.get(j), rules)) {
                    matchedActual[j] = true;
                    found = true;
                    recordMatch(path.index(i), nodeToValue(expItem));
                    break;
                }
            }

            if (!found) {
                recordRemoved(path.index(i), nodeToValue(expItem));
            }
        }

        for (int j = 0; j < actualList.size(); j++) {
            if (!matchedActual[j]) {
                recordAdded(path.index(j), nodeToValue(actualList.get(j)));
            }
        }
    }

    private boolean nodesEqual(JsonNode a, JsonNode b, ComparisonRules rules) {
        if (a.equals(b))
            return true;
        if (a.isValueNode() && b.isValueNode()) {
            return rules.areValuesEqual(nodeToValue(a), nodeToValue(b));
        }
        // For complex types, use string comparison as fallback
        return a.toString().equals(b.toString());
    }

    private void compareValues(JsonNode expected, JsonNode actual, FieldPath path, ComparisonRules rules) {
        Object expValue = nodeToValue(expected);
        Object actValue = nodeToValue(actual);

        if (rules.areValuesEqual(expValue, actValue)) {
            recordMatch(path, expValue);
        } else {
            recordModified(path, expValue, actValue);
        }
    }

    private Object nodeToValue(JsonNode node) {
        if (node == null || node.isNull())
            return null;
        if (node.isTextual())
            return node.asText();
        if (node.isBoolean())
            return node.asBoolean();
        if (node.isInt())
            return node.asInt();
        if (node.isLong())
            return node.asLong();
        if (node.isDouble() || node.isFloat())
            return node.asDouble();
        if (node.isArray() || node.isObject())
            return node.toString();
        return node.asText();
    }
}
