package com.smartfitness.monitoring.service;

/**
 * Business Layer: Fault Detection Service Interface
 * Reference: 05_MonitoringServiceComponent.puml (IFaultDetectionService)
 */
public interface IFaultDetectionService {
    void detectFault(String equipmentId, String faultReason);
}

