package com.smartfitness.monitoring.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Equipment Command API Interface
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentCommandApi)
 */
public interface IEquipmentCommandApi {
    ResponseEntity<Map<String, Object>> sendPingRequest(String equipmentId);
    ResponseEntity<Map<String, Object>> getEquipmentStatus(String equipmentId);
}

