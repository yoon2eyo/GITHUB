package com.smartfitness.facemodel.model;

import com.smartfitness.event.DomainEvent;
import java.time.LocalDateTime;

public class FaceVerificationEvent implements DomainEvent {
    private String modelId;
    private String userId;
    private String deviceId;
    private LocalDateTime timestamp;
    private VerificationResult result;
    private double confidence;

    public enum VerificationResult {
        VERIFIED,
        NOT_VERIFIED,
        ERROR
    }

    public FaceVerificationEvent(String modelId, String userId, String deviceId) {
        this.modelId = modelId;
        this.userId = userId;
        this.deviceId = deviceId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getModelId() { return modelId; }
    public String getUserId() { return userId; }
    public String getDeviceId() { return deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public VerificationResult getResult() { return result; }
    public double getConfidence() { return confidence; }
    
    // Setters
    public void setResult(VerificationResult result) { this.result = result; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}