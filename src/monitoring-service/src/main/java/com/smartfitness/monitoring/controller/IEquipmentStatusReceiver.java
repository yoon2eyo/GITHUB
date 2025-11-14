package com.smartfitness.monitoring.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Equipment Status Receiver Interface
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentStatusReceiver)
 */
public interface IEquipmentStatusReceiver {
    ResponseEntity<Map<String, String>> receiveHeartbeat(String equipmentId, String status);
}

