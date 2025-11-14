package com.smartfitness.monitoring.service;

import com.smartfitness.monitoring.repository.IEquipmentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Business Layer: Heartbeat Receiver
 * Component: HeartbeatReceiver
 * 
 * DD-04 Tactic: Heartbeat
 * - Equipment sends status every 10 minutes
 * - If '고장' status received → Immediate fault detection via FaultDetector
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatReceiver implements IHeartbeatReceiverService {
    
    private final IFaultDetectionService faultDetectionService;
    private final IEquipmentStatusRepository equipmentStatusRepository;
    
    @Override
    public void processHeartbeat(String equipmentId, String status) {
        log.info("Processing heartbeat: equipmentId={}, status={}", equipmentId, status);
        
        // 1. Save heartbeat status
        equipmentStatusRepository.saveHeartbeat(equipmentId, status, LocalDateTime.now());
        
        // 2. Check for fault status
        if ("고장".equalsIgnoreCase(status) || "FAULT".equalsIgnoreCase(status)) {
            log.warn("Fault status detected for equipment: {}", equipmentId);
            faultDetectionService.detectFault(equipmentId, "Fault status reported via heartbeat");
        }
        
        log.debug("Heartbeat processed successfully for equipment: {}", equipmentId);
    }
}

