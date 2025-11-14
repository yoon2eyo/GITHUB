package com.smartfitness.monitoring.adapter;

/**
 * System Interface Layer: Equipment Gateway Interface
 * Reference: 05_MonitoringServiceComponent.puml (IEquipmentGateway)
 */
public interface IEquipmentGateway {
    boolean sendPing(String equipmentId);
}

