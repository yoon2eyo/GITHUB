package com.smartfitness.monitoring.service;

import com.smartfitness.monitoring.repository.IEquipmentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Business Layer: Equipment Health Checker
 * Component: EquipmentHealthChecker
 * 
 * DD-04 Tactic: Ping/echo
 * - Triggered by Timer every 10 seconds (via ISchedulerService)
 * - Checks if no heartbeat for 30 seconds
 * - If timeout → Send ping via PingEchoExecutor
 * - If no response → Fault detected via FaultDetector
 * 
 * QAS-01: Alert within 15초
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EquipmentHealthChecker {
    
    private final IPingEchoService pingEchoService;
    private final IFaultDetectionService faultDetectionService;
    private final IEquipmentStatusRepository equipmentStatusRepository;
    
    private static final int HEARTBEAT_TIMEOUT_SECONDS = 30;
    
    /**
     * DD-04: Check equipment health every 10 seconds
     * Scheduled by Quartz (ISchedulerService)
     */
    @Scheduled(fixedDelay = 10000) // 10 seconds
    public void checkEquipmentHealth() {
        log.trace("Starting equipment health check cycle");
        
        // 1. Check last heartbeat for all equipment
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);
        List<String> timedOutEquipment = equipmentStatusRepository.findEquipmentWithNoHeartbeatSince(timeoutThreshold);
        
        if (timedOutEquipment.isEmpty()) {
            log.trace("All equipment responding normally");
            return;
        }
        
        log.warn("Found {} equipment with timeout", timedOutEquipment.size());
        
        // 2. For each timed-out equipment, send ping
        for (String equipmentId : timedOutEquipment) {
            log.info("Sending ping to timed-out equipment: {}", equipmentId);
            
            boolean isResponding = pingEchoService.sendPing(equipmentId);
            
            // 3. If no response → Detect fault
            if (!isResponding) {
                log.error("Equipment not responding to ping: {}", equipmentId);
                faultDetectionService.detectFault(
                        equipmentId, 
                        "No heartbeat for " + HEARTBEAT_TIMEOUT_SECONDS + " seconds and no ping response"
                );
            }
        }
        
        log.debug("Equipment health check cycle completed");
    }
}

