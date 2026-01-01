package com.diffmaster.exception;

/**
 * Thrown when a comparison rule is invalid.
 */
public class InvalidRuleException extends DiffMasterException {
    private final String ruleName;

    public InvalidRuleException(String ruleName, String message) {
        super("Invalid rule '" + ruleName + "': " + message);
        this.ruleName = ruleName;
    }

    public String getRuleName() {
        return ruleName;
    }
}
