package com.smartfitness.facemodel.events;

import com.smartfitness.messaging.event.IDomainEvent;
import java.util.UUID;

public class FaceVerificationEvent implements IDomainEvent {
    private final String eventId;
    private final String modelId;
    private final String userId;
    private final String deviceId;
    private final long timestamp;
    private VerificationResult result;
    private double confidence;

    public enum VerificationResult {
        VERIFIED,
        NOT_VERIFIED,
        ERROR
    }

    public FaceVerificationEvent(String modelId, String userId, String deviceId) {
        this.eventId = UUID.randomUUID().toString();
        this.modelId = modelId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public String getEventId() { return eventId; }

    @Override
    public long getTimestamp() { return timestamp; }

    @Override
    public String getEventType() { return "FACE_VERIFICATION"; }

    // Additional getters
    public String getModelId() { return modelId; }
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public VerificationResult getResult() { return result; }
    public double getConfidence() { return confidence; }
    
    // Setters
    public void setResult(VerificationResult result) { this.result = result; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}