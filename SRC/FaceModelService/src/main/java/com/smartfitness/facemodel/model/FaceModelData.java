package com.smartfitness.facemodel.model;

import java.time.LocalDateTime;

/**
 * 얼굴 모델 데이터 모델
 */
public class FaceModelData {
    private String vectorId;
    private String userId;
    private byte[] faceVector;
    private String modelVersion;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private Integer usageCount;
    private Boolean isActive;

    public FaceModelData() {
    }

    public FaceModelData(String userId, byte[] faceVector, String modelVersion) {
        this.userId = userId;
        this.faceVector = faceVector;
        this.modelVersion = modelVersion;
        this.createdAt = LocalDateTime.now();
        this.usageCount = 0;
        this.isActive = true;
    }

    // Getters and Setters
    public String getVectorId() { return vectorId; }
    public void setVectorId(String vectorId) { this.vectorId = vectorId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public byte[] getFaceVector() { return faceVector; }
    public void setFaceVector(byte[] faceVector) { this.faceVector = faceVector; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    public Integer getUsageCount() { return usageCount; }
    public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
