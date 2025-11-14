package com.smartfitness.monitoring.service;

import com.smartfitness.common.event.EquipmentFaultEvent;
import com.smartfitness.monitoring.adapter.IMessagePublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Business Layer: Fault Detector
 * Component: FaultDetector
 * 
 * DD-04: Fault Detection
 * - Detects equipment faults from both Heartbeat and Ping/echo
 * - Publishes EquipmentFaultEvent → NotificationDispatcher
 * - Maintains audit trail
 * 
 * QAS-01: Alert within 15초
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaultDetector implements IFaultDetectionService {
    
    private final IMessagePublisherService messagePublisherService;
    private final IAuditLogService auditLogService;
    
    @Override
    public void detectFault(String equipmentId, String faultReason) {
        log.error("Fault detected: equipmentId={}, reason={}", equipmentId, faultReason);
        
        // 1. Publish EquipmentFaultEvent (Passive Redundancy)
        EquipmentFaultEvent event = new EquipmentFaultEvent(
                equipmentId, 
                faultReason, 
                LocalDateTime.now()
        );
        messagePublisherService.publishEvent(event);
        log.info("EquipmentFaultEvent published: {}", equipmentId);
        
        // 2. Maintain audit trail
        auditLogService.logFaultDetection(equipmentId, faultReason);
        
        log.info("Fault detection completed for equipment: {}", equipmentId);
    }
}

