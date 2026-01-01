package com.diffmaster.exception;

/**
 * Thrown when comparison fails.
 */
public class ComparisonException extends DiffMasterException {
    public ComparisonException(String message) {
        super(message);
    }

    public ComparisonException(String message, Throwable cause) {
        super(message, cause);
    }
}
