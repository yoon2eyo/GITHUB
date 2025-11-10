package com.smartfitness.access.model;

import java.time.LocalDateTime;

public class AccessAttempt {
    private String userId;
    private String deviceId;
    private LocalDateTime timestamp;
    private AccessStatus status;
    private String verificationResult;

    public enum AccessStatus {
        GRANTED,
        DENIED,
        PENDING,
        ERROR
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public AccessStatus getStatus() { return status; }
    public void setStatus(AccessStatus status) { this.status = status; }
    public String getVerificationResult() { return verificationResult; }
    public void setVerificationResult(String verificationResult) { this.verificationResult = verificationResult; }
}