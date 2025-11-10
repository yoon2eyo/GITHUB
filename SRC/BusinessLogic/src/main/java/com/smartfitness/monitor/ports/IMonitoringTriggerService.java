package com.smartfitness.monitor.ports;

/**
 * IMonitoringTriggerService: Timer-driven trigger to run heartbeat checks.
 */
public interface IMonitoringTriggerService {
    /**
     * Trigger a monitoring cycle across equipment (UC-21).
     */
    void triggerMonitorCheck();
}

