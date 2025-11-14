package com.smartfitness.monitoring.service;

import com.smartfitness.monitoring.adapter.IEquipmentGateway;
import com.smartfitness.monitoring.repository.IEquipmentStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Business Layer: Ping/Echo Executor
 * Component: PingEchoExecutor
 * 
 * DD-04 Tactic: Ping/echo
 * - Sends ping/status request to equipment
 * - Updates equipment status based on response
 * 
 * Reference: 05_MonitoringServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PingEchoExecutor implements IPingEchoService {
    
    private final IEquipmentGateway equipmentGateway;
    private final IEquipmentStatusRepository equipmentStatusRepository;
    
    @Override
    public boolean sendPing(String equipmentId) {
        log.debug("Sending ping to equipment: {}", equipmentId);
        
        try {
            // 1. Send ping request via Equipment Gateway
            boolean isResponding = equipmentGateway.sendPing(equipmentId);
            
            // 2. Update equipment status
            String status = isResponding ? "RESPONDING" : "NO_RESPONSE";
            equipmentStatusRepository.updatePingStatus(equipmentId, status, LocalDateTime.now());
            
            log.debug("Ping completed: equipmentId={}, responding={}", equipmentId, isResponding);
            
            return isResponding;
            
        } catch (Exception e) {
            log.error("Failed to ping equipment: {}", equipmentId, e);
            return false;
        }
    }
}

