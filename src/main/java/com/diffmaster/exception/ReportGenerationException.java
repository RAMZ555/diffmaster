package com.diffmaster.exception;

/**
 * Thrown when report generation fails.
 */
public class ReportGenerationException extends DiffMasterException {
    public ReportGenerationException(String message) {
        super(message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
