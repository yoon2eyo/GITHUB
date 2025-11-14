package com.smartfitness.monitoring.service;

/**
 * Business Layer: Audit Log Service Interface
 * Reference: 05_MonitoringServiceComponent.puml (IAuditLogService)
 */
public interface IAuditLogService {
    void logFaultDetection(String equipmentId, String faultReason);
    void logHeartbeat(String equipmentId, String status);
    void logPingEcho(String equipmentId, boolean isResponding);
}

