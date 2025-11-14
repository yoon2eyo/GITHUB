package com.smartfitness.monitoring.controller;

import com.smartfitness.monitoring.service.IHeartbeatReceiverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Equipment Status Receiver
 * Component: EquipmentStatusReceiver
 * 
 * DD-04 Tactic: Heartbeat
 * - Equipment sends status every 10 minutes
 * - If '고장' status received → Immediate fault detection
 * 
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentStatusReceiver)
 */
@Slf4j
@RestController
@RequestMapping("/monitoring/equipment/status")
@RequiredArgsConstructor
public class EquipmentStatusReceiver implements IEquipmentStatusReceiver {
    
    private final IHeartbeatReceiverService heartbeatReceiverService;
    
    /**
     * DD-04: Receive heartbeat from equipment
     * Equipment reports status every 10 minutes
     */
    @Override
    @PostMapping("/heartbeat")
    public ResponseEntity<Map<String, String>> receiveHeartbeat(
            @RequestParam String equipmentId,
            @RequestParam String status) {
        
        log.info("Heartbeat received: equipmentId={}, status={}", equipmentId, status);
        
        heartbeatReceiverService.processHeartbeat(equipmentId, status);
        
        return ResponseEntity.ok(Map.of(
                "message", "Heartbeat received",
                "equipmentId", equipmentId
        ));
    }
}

