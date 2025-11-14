package com.smartfitness.access.service;

/**
 * Business Layer Interface: Gate Control Service
 * Physical gate control operations
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IGateControlService {
    boolean openGate(String equipmentId);
    boolean closeGate(String equipmentId);
    String getGateStatus(String equipmentId);
}

