package com.smartfitness.access.adapter;

/**
 * System Interface Layer: Equipment Gateway Interface
 * Communicates with physical equipment (gates, cameras)
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IEquipmentGateway {
    boolean sendCommand(String equipmentId, String command);
    String getStatus(String equipmentId);
}

