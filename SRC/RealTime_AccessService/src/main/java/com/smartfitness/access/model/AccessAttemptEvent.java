package com.smartfitness.access.model;

import com.smartfitness.event.DomainEvent;
import java.time.LocalDateTime;

public class AccessAttemptEvent implements DomainEvent {
    private String userId;
    private String deviceId;
    private LocalDateTime timestamp;
    private AccessAttemptType eventType;
    private String result;

    public enum AccessAttemptType {
        ACCESS_ATTEMPTED,
        ACCESS_GRANTED,
        ACCESS_DENIED,
        VERIFICATION_FAILED
    }

    public AccessAttemptEvent(String userId, String deviceId, AccessAttemptType eventType) {
        this.userId = userId;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public AccessAttemptType getEventType() { return eventType; }
    public String getResult() { return result; }
    
    // Setters
    public void setResult(String result) { this.result = result; }
}