package com.smartfitness.monitoring.service;

import com.smartfitness.monitoring.repository.IEquipmentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Business Layer: Audit Logger
 * Component: AuditLogger
 * 
 * DD-04 Tactic: Maintain Audit Trail
 * - Logs all monitoring events for audit purposes
 * - Persists audit logs via IEquipmentStatusRepository
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogger implements IAuditLogService {
    
    private final IEquipmentStatusRepository equipmentStatusRepository;
    
    @Override
    public void logFaultDetection(String equipmentId, String faultReason) {
        log.info("Audit: Fault detected - equipmentId={}, reason={}", equipmentId, faultReason);
        
        // Persist audit log
        equipmentStatusRepository.saveAuditLog(
                equipmentId, 
                "FAULT_DETECTED", 
                faultReason, 
                LocalDateTime.now()
        );
    }
    
    @Override
    public void logHeartbeat(String equipmentId, String status) {
        log.debug("Audit: Heartbeat received - equipmentId={}, status={}", equipmentId, status);
        
        // Stub: Optionally persist audit log for heartbeats
    }
    
    @Override
    public void logPingEcho(String equipmentId, boolean isResponding) {
        log.debug("Audit: Ping/echo - equipmentId={}, responding={}", equipmentId, isResponding);
        
        // Stub: Optionally persist audit log for ping/echo
    }
}

