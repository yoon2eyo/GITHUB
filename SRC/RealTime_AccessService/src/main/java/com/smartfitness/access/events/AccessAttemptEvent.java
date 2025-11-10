package com.smartfitness.access.events;

import com.smartfitness.messaging.event.IDomainEvent;
import java.util.UUID;

public class AccessAttemptEvent implements IDomainEvent {
    private final String eventId;
    private final String userId;
    private final String deviceId;
    private final long timestamp;
    private final AccessAttemptType eventType;
    private String verificationResult;

    public enum AccessAttemptType {
        ACCESS_ATTEMPTED,
        ACCESS_GRANTED,
        ACCESS_DENIED,
        VERIFICATION_FAILED
    }

    public AccessAttemptEvent(String userId, String deviceId, AccessAttemptType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public String getEventType() { return eventType.name(); }

    // Additional getters
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public String getVerificationResult() { return verificationResult; }
    
    // Setters
    public void setVerificationResult(String verificationResult) {
        this.verificationResult = verificationResult;
    }
}