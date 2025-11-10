package com.smartfitness.access.model;

/**
 * AccessGrantResult: Outcome of access authorization.
 */
public class AccessGrantResult {
    private final boolean granted;
    private final String message;

    private AccessGrantResult(boolean granted, String message) {
        this.granted = granted;
        this.message = message;
    }

    public static AccessGrantResult GRANTED(String message) {
        return new AccessGrantResult(true, message);
    }

    public static AccessGrantResult DENIED(String message) {
        return new AccessGrantResult(false, message);
    }

    public boolean isGranted() {
        return granted;
    }

    public String getMessage() {
        return message;
    }
}
