package com.diffmaster.comparator;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.rules.ComparisonRules;
import com.diffmaster.rules.IgnoreFieldRule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonComparatorTest {

    private final JsonComparator comparator = new JsonComparator();

    @Test
    void testPartialMatch() {
        String expected = "{\"id\": 1, \"name\": \"Alice\"}";
        String actual = "{\"id\": 1, \"name\": \"Bob\"}";

        ComparisonResult result = comparator.compare(expected, actual, ComparisonRules.strict());

        assertFalse(result.isMatch());
        assertEquals(1, result.getDifferences().size());
        assertEquals("name", result.getDifferences().get(0).getPath().getLastSegment());
    }

    @Test
    void testIgnoreField() {
        String expected = "{\"id\": 1, \"name\": \"Alice\"}";
        String actual = "{\"id\": 1, \"name\": \"Bob\"}";

        ComparisonRules rules = ComparisonRules.strict().addRule(new IgnoreFieldRule("name"));
        ComparisonResult result = comparator.compare(expected, actual, rules);

        assertTrue(result.isMatch());
    }
}
