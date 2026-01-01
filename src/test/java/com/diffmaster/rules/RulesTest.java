package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RulesTest {

    @Test
    void testNumericTolerance() {
        NumericToleranceRule rule = new NumericToleranceRule(0.01);
        assertTrue(rule.areEqual(1.000, 1.009)); // Diff 0.009 <= 0.01
        assertFalse(rule.areEqual(1.000, 1.011)); // Diff 0.011 > 0.01
    }

    @Test
    void testDateTolerance() {
        DateToleranceRule rule = new DateToleranceRule(Duration.ofSeconds(60));
        String t1 = "2023-01-01T12:00:00Z";
        String t2 = "2023-01-01T12:00:59Z";
        String t3 = "2023-01-01T12:01:01Z";

        assertTrue(rule.areEqual(t1, t2));
        assertFalse(rule.areEqual(t1, t3));
    }

    @Test
    void testTypeCoercion() {
        TypeCoercionRule rule = new TypeCoercionRule();
        assertTrue(rule.areEqual("123", 123));
        assertTrue(rule.areEqual("true", true));
        assertFalse(rule.areEqual("123", 124));
    }

    @Test
    void testIgnorePattern() {
        IgnorePatternRule rule = new IgnorePatternRule(".*_id");
        assertTrue(rule.appliesTo(FieldPath.of("customer_id")));
        assertFalse(rule.appliesTo(FieldPath.of("customer_name")));
    }
}
