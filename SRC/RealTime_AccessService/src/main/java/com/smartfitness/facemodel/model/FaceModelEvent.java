package com.smartfitness.facemodel.model;

import java.time.LocalDateTime;

public class FaceModelEvent {
    private String modelId;
    private String userId;
    private EventType eventType;
    private LocalDateTime timestamp;
    private String details;

    public enum EventType {
        MODEL_UPDATED,
        MODEL_VERIFIED,
        MODEL_FAILED,
        VERIFICATION_ATTEMPTED
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}