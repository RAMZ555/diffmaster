package com.diffmaster.rules;

import com.diffmaster.core.FieldPath;
import lombok.Getter;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.List;

/**
 * Rule for date/time comparison with tolerance.
 * Example: timestamps within 5 seconds are considered equal.
 */
@Getter
public class DateToleranceRule implements Rule {
    private final Duration tolerance;

    private static final List<DateTimeFormatter> COMMON_FORMATS = List.of(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_ZONED_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    public DateToleranceRule(Duration tolerance) {
        this.tolerance = tolerance;
    }

    public static DateToleranceRule ofSeconds(long seconds) {
        return new DateToleranceRule(Duration.ofSeconds(seconds));
    }

    public static DateToleranceRule ofMinutes(long minutes) {
        return new DateToleranceRule(Duration.ofMinutes(minutes));
    }

    public static DateToleranceRule ofHours(long hours) {
        return new DateToleranceRule(Duration.ofHours(hours));
    }

    @Override
    public String getName() {
        return "DateTolerance";
    }

    @Override
    public boolean appliesTo(FieldPath path) {
        return true;
    }

    @Override
    public boolean shouldIgnore(FieldPath path, Object expected, Object actual) {
        return areEqual(expected, actual);
    }

    @Override
    public boolean areEqual(Object expected, Object actual) {
        Instant exp = toInstant(expected);
        Instant act = toInstant(actual);
        if (exp == null || act == null) {
            return false;
        }
        Duration diff = Duration.between(exp, act).abs();
        return diff.compareTo(tolerance) <= 0;
    }

    private Instant toInstant(Object value) {
        if (value == null)
            return null;
        if (value instanceof Instant)
            return (Instant) value;
        if (value instanceof Temporal) {
            try {
                return Instant.from((Temporal) value);
            } catch (Exception e) {
                // Try other conversions
            }
        }

        String str = value.toString();
        // Try epoch millis
        try {
            long millis = Long.parseLong(str);
            return Instant.ofEpochMilli(millis);
        } catch (NumberFormatException ignored) {
        }

        // Try common date formats
        for (DateTimeFormatter formatter : COMMON_FORMATS) {
            try {
                return ZonedDateTime.parse(str, formatter).toInstant();
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalDateTime.parse(str, formatter).atZone(ZoneId.systemDefault()).toInstant();
            } catch (DateTimeParseException ignored) {
            }
        }

        // Try Instant.parse directly
        try {
            return Instant.parse(str);
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }
}
