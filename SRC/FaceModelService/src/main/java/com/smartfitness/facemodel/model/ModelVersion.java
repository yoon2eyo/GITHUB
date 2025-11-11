package com.smartfitness.facemodel.model;

import java.time.LocalDateTime;

/**
 * 모델 버전 정보 모델
 */
public class ModelVersion {
    private String version;
    private byte[] modelBinary;
    private String accuracy;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime deployedAt;
    private Boolean isActive;
    private String trainingDataSet;
    private Integer modelSize;

    public ModelVersion() {
    }

    public ModelVersion(String version, byte[] modelBinary, String accuracy) {
        this.version = version;
        this.modelBinary = modelBinary;
        this.accuracy = accuracy;
        this.createdAt = LocalDateTime.now();
        this.isActive = false;
    }

    // Getters and Setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public byte[] getModelBinary() { return modelBinary; }
    public void setModelBinary(byte[] modelBinary) { this.modelBinary = modelBinary; }
    public String getAccuracy() { return accuracy; }
    public void setAccuracy(String accuracy) { this.accuracy = accuracy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDeployedAt() { return deployedAt; }
    public void setDeployedAt(LocalDateTime deployedAt) { this.deployedAt = deployedAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public String getTrainingDataSet() { return trainingDataSet; }
    public void setTrainingDataSet(String trainingDataSet) { this.trainingDataSet = trainingDataSet; }
    public Integer getModelSize() { return modelSize; }
    public void setModelSize(Integer modelSize) { this.modelSize = modelSize; }
}
