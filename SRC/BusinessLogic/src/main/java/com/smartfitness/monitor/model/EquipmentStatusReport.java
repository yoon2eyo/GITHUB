package com.smartfitness.monitor.model;

import java.util.Date;

/**
 * EquipmentStatusReport: Heartbeat/status payload from field equipment.
 */
public class EquipmentStatusReport {
    private final String equipmentId;
    private final Date reportedAt;
    private final boolean fault;
    private final String details;

    public EquipmentStatusReport(String equipmentId, Date reportedAt, boolean fault, String details) {
        this.equipmentId = equipmentId;
        this.reportedAt = reportedAt;
        this.fault = fault;
        this.details = details;
    }

    public String getEquipmentId() { return equipmentId; }
    public Date getReportedAt() { return reportedAt; }
    public boolean isFault() { return fault; }
    public String getDetails() { return details; }
}

