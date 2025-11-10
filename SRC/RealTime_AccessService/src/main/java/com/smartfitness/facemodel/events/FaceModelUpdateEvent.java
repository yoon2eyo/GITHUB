package com.smartfitness.facemodel.events;

import com.smartfitness.messaging.event.IDomainEvent;
import java.util.UUID;

public class FaceModelUpdateEvent implements IDomainEvent {
    private final String eventId;
    private final String modelId;
    private final String userId;
    private final long timestamp;
    private final ModelUpdateType eventType;
    private String details;

    public enum ModelUpdateType {
        MODEL_TRAINED,
        MODEL_VERIFIED,
        MODEL_VALIDATION_FAILED,
        MODEL_DEPLOYED
    }

    public FaceModelUpdateEvent(String modelId, String userId, ModelUpdateType eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.modelId = modelId;
        this.userId = userId;
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
    public String getModelId() { return modelId; }
    public String getUserId() { return userId; }
    public String getDetails() { return details; }
    
    // Setters
    public void setDetails(String details) { this.details = details; }
}