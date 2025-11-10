package com.smartfitness.monitor.ports;

import com.smartfitness.monitor.model.EquipmentStatusReport;
import java.util.Date;

/**
 * IMonitorRepository: Access to DB_MONITOR for heartbeat records and alerts.
 */
public interface IMonitorRepository {
    void saveStatus(EquipmentStatusReport report);
    Date findLastReportTime(String equipmentId);
    void saveNotificationLog(String equipmentId, String alertMessage);
}

