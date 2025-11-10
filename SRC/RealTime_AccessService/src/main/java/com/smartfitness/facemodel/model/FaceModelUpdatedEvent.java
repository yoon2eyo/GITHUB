package com.smartfitness.facemodel.model;

import com.smartfitness.event.DomainEvent;
import java.time.LocalDateTime;

public class FaceModelUpdatedEvent implements DomainEvent {
    private String modelId;
    private String userId;
    private LocalDateTime timestamp;
    private ModelUpdateType updateType;
    private String details;

    public enum ModelUpdateType {
        MODEL_TRAINED,
        MODEL_VERIFIED,
        MODEL_VALIDATION_FAILED,
        MODEL_DEPLOYED
    }

    public FaceModelUpdatedEvent(String modelId, String userId, ModelUpdateType updateType) {
        this.modelId = modelId;
        this.userId = userId;
        this.updateType = updateType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getModelId() { return modelId; }
    public String getUserId() { return userId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public ModelUpdateType getUpdateType() { return updateType; }
    public String getDetails() { return details; }
    
    // Setters
    public void setDetails(String details) { this.details = details; }
}