package com.diffmaster.exception;

/**
 * Thrown when an unsupported file format is encountered.
 */
public class UnsupportedFormatException extends DiffMasterException {
    private final String format;

    public UnsupportedFormatException(String format) {
        super("Unsupported format: " + format);
        this.format = format;
    }

    public UnsupportedFormatException(String format, String message) {
        super(message);
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
