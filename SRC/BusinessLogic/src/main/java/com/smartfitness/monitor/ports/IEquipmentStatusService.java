package com.smartfitness.monitor.ports;

import com.smartfitness.monitor.model.EquipmentStatusReport;

/**
 * IEquipmentStatusService: Receives periodic heartbeats/status reports from equipment.
 */
public interface IEquipmentStatusService {
    /**
     * Receive and persist a heartbeat/status report (UC-20).
     */
    void receiveStatusReport(EquipmentStatusReport report);
}

