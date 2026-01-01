package com.diffmaster.exception;

/**
 * Base exception for all DiffMaster errors.
 */
public class DiffMasterException extends RuntimeException {
    public DiffMasterException(String message) {
        super(message);
    }

    public DiffMasterException(String message, Throwable cause) {
        super(message, cause);
    }
}
