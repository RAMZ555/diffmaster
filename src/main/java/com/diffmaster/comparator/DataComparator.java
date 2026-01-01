package com.diffmaster.comparator;

import com.diffmaster.core.ComparisonResult;
import com.diffmaster.input.DataSource;
import com.diffmaster.rules.ComparisonRules;

/**
 * Base interface for data comparators.
 */
public interface DataComparator {
    /**
     * Get the format this comparator handles.
     */
    String getFormat();

    /**
     * Check if this comparator can handle the given format.
     */
    boolean supports(String format);

    /**
     * Compare two data sources.
     */
    ComparisonResult compare(DataSource expected, DataSource actual, ComparisonRules rules);

    /**
     * Compare two strings of content.
     */
    ComparisonResult compare(String expected, String actual, ComparisonRules rules);
}
