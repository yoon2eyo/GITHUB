package com.smartfitness.monitor.model;

import java.time.LocalDateTime;

public class EquipmentStatus {
    private String equipmentId;
    private EquipmentStatusType status;
    private LocalDateTime lastUpdateTime;
    private String lastFaultReason;

    public enum EquipmentStatusType {
        ACTIVE,
        INACTIVE,
        FAULT,
        TIMEOUT
    }

    // Getters and setters
    public String getEquipmentId() { return equipmentId; }
    public void setEquipmentId(String equipmentId) { this.equipmentId = equipmentId; }
    public EquipmentStatusType getStatus() { return status; }
    public void setStatus(EquipmentStatusType status) { this.status = status; }
    public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
    public String getLastFaultReason() { return lastFaultReason; }
    public void setLastFaultReason(String lastFaultReason) { this.lastFaultReason = lastFaultReason; }
}