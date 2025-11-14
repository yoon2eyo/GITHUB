package com.smartfitness.monitoring.service;

/**
 * Business Layer: Ping/Echo Service Interface
 * Reference: 05_MonitoringServiceComponent.puml (IPingEchoService)
 */
public interface IPingEchoService {
    boolean sendPing(String equipmentId);
}

