package com.smartfitness.event;

/**
 * AccessAttemptEvent: Emitted after an access authorization attempt.
 */
public class AccessAttemptEvent implements DomainEvent {
    private final String faceId;
    private final boolean granted;

    public AccessAttemptEvent(String faceId, boolean granted) {
        this.faceId = faceId;
        this.granted = granted;
    }

    public String getFaceId() { return faceId; }
    public boolean isGranted() { return granted; }
}

