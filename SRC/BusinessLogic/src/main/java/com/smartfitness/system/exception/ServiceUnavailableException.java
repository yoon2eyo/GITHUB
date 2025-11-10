package com.smartfitness.system.exception;

/**
 * ServiceUnavailableException: Indicates downstream service resolution or availability failure.
 */
public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) { super(message); }
    public ServiceUnavailableException(String message, Throwable cause) { super(message, cause); }
}

