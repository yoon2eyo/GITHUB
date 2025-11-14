package com.smartfitness.access.service;

import com.smartfitness.access.adapter.IEquipmentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Gate Controller
 * Component: GateController
 * Controls physical gate devices (open/close commands)
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GateController implements IGateControlService {
    
    private final IEquipmentGateway equipmentGateway;
    
    @Override
    public boolean openGate(String equipmentId) {
        log.info("Opening gate: {}", equipmentId);
        
        boolean success = equipmentGateway.sendCommand(equipmentId, "OPEN");
        
        if (success) {
            log.info("Gate opened successfully: {}", equipmentId);
        } else {
            log.error("Failed to open gate: {}", equipmentId);
        }
        
        return success;
    }
    
    @Override
    public boolean closeGate(String equipmentId) {
        log.info("Closing gate: {}", equipmentId);
        
        boolean success = equipmentGateway.sendCommand(equipmentId, "CLOSE");
        
        if (success) {
            log.info("Gate closed successfully: {}", equipmentId);
        } else {
            log.error("Failed to close gate: {}", equipmentId);
        }
        
        return success;
    }
    
    @Override
    public String getGateStatus(String equipmentId) {
        log.debug("Checking gate status: {}", equipmentId);
        
        return equipmentGateway.getStatus(equipmentId);
    }
}

