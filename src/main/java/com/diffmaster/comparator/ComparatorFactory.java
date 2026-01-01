package com.diffmaster.comparator;

import com.diffmaster.exception.UnsupportedFormatException;
import com.diffmaster.input.DataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create comparators based on format.
 */
public class ComparatorFactory {
    private static final Map<String, DataComparator> comparators = new HashMap<>();

    static {
        register(new JsonComparator());
        register(new XmlComparator());
        register(new CsvComparator());
        register(new YamlComparator());
        register(new TextComparator());
        register(new PropertiesComparator());
        register(new ExcelComparator());
    }

    /**
     * Register a custom comparator.
     */
    public static void register(DataComparator comparator) {
        comparators.put(comparator.getFormat().toLowerCase(), comparator);
    }

    /**
     * Get comparator for the given format.
     */
    public static DataComparator getComparator(String format) {
        String key = format.toLowerCase();
        DataComparator comparator = comparators.get(key);

        if (comparator == null) {
            // Try to find by supports
            for (DataComparator c : comparators.values()) {
                if (c.supports(format)) {
                    return c;
                }
            }
            throw new UnsupportedFormatException(format);
        }

        return comparator;
    }

    /**
     * Auto-detect format and get comparator.
     */
    public static DataComparator autoDetect(DataSource source) {
        String hint = source.getFormatHint();
        if (hint != null) {
            return getComparator(hint);
        }

        // Try to detect from content
        try {
            String content = source.getContent();
            if (content != null) {
                String trimmed = content.trim();

                if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                    return getComparator("json");
                }
                if (trimmed.startsWith("<")) {
                    return getComparator("xml");
                }
                if (trimmed.contains(":") && !trimmed.contains(",")) {
                    // Might be YAML
                    return getComparator("yaml");
                }
                if (trimmed.contains(",")) {
                    return getComparator("csv");
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }

        // Default to text
        return getComparator("text");
    }

    /**
     * Auto-detect format from two sources (prefer matching format).
     */
    public static DataComparator autoDetect(DataSource expected, DataSource actual) {
        String expHint = expected.getFormatHint();
        String actHint = actual.getFormatHint();

        if (expHint != null && expHint.equalsIgnoreCase(actHint)) {
            return getComparator(expHint);
        }

        if (expHint != null) {
            return getComparator(expHint);
        }

        if (actHint != null) {
            return getComparator(actHint);
        }

        return autoDetect(expected);
    }

    /**
     * Check if a format is supported.
     */
    public static boolean isSupported(String format) {
        if (comparators.containsKey(format.toLowerCase())) {
            return true;
        }
        for (DataComparator c : comparators.values()) {
            if (c.supports(format)) {
                return true;
            }
        }
        return false;
    }
}
