package com.smartfitness.monitoring.repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * System Interface Layer: Equipment Status Repository Interface
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentStatusRepository)
 */
public interface IEquipmentStatusRepository {
    void saveHeartbeat(String equipmentId, String status, LocalDateTime timestamp);
    void updatePingStatus(String equipmentId, String status, LocalDateTime timestamp);
    List<String> findEquipmentWithNoHeartbeatSince(LocalDateTime threshold);
    void saveAuditLog(String equipmentId, String eventType, String details, LocalDateTime timestamp);
}

