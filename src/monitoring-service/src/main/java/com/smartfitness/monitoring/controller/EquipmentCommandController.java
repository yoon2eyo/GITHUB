package com.smartfitness.monitoring.controller;

import com.smartfitness.monitoring.service.IPingEchoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Equipment Command Controller
 * Component: EquipmentCommandController
 * 
 * DD-04 Tactic: Ping/echo
 * - Sends ping/status requests to equipment
 * - Triggered by EquipmentHealthChecker
 * 
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentCommandApi)
 */
@Slf4j
@RestController
@RequestMapping("/monitoring/equipment/command")
@RequiredArgsConstructor
public class EquipmentCommandController implements IEquipmentCommandApi {
    
    private final IPingEchoService pingEchoService;
    
    /**
     * DD-04: Send ping request to equipment
     * Triggered when no heartbeat for 30 seconds
     */
    @Override
    @PostMapping("/ping")
    public ResponseEntity<Map<String, Object>> sendPingRequest(@RequestParam String equipmentId) {
        log.info("Sending ping request to equipment: {}", equipmentId);
        
        boolean isAlive = pingEchoService.sendPing(equipmentId);
        
        return ResponseEntity.ok(Map.of(
                "equipmentId", equipmentId,
                "isAlive", isAlive,
                "message", isAlive ? "Equipment responding" : "No response"
        ));
    }
    
    /**
     * Get equipment status
     */
    @Override
    @GetMapping("/{equipmentId}/status")
    public ResponseEntity<Map<String, Object>> getEquipmentStatus(@PathVariable String equipmentId) {
        log.info("Get equipment status: {}", equipmentId);
        
        // Stub: Return equipment status
        return ResponseEntity.ok(Map.of(
                "equipmentId", equipmentId,
                "status", "NORMAL",
                "lastHeartbeat", System.currentTimeMillis()
        ));
    }
}

