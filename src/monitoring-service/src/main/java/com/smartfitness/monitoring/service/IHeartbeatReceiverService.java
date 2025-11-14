package com.smartfitness.monitoring.service;

/**
 * Business Layer: Heartbeat Receiver Service Interface
 * Reference: 05_MonitoringServiceComponent.puml (IHeartbeatReceiverService)
 */
public interface IHeartbeatReceiverService {
    void processHeartbeat(String equipmentId, String status);
}

