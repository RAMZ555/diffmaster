package com.diffmaster.core;

/**
 * Severity level of a difference.
 */
public enum DiffSeverity {
    /** Informational - cosmetic or minor differences */
    INFO,
    /** Warning - might indicate an issue */
    WARNING,
    /** Error - significant difference that likely indicates a problem */
    ERROR
}
