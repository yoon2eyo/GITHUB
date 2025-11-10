package com.smartfitness.event;

/**
 * EquipmentFaultDetectedEvent: Raised when a device reports a fault or times out.
 */
public class EquipmentFaultDetectedEvent implements DomainEvent {
    private final String equipmentId;
    private final String reason;

    public EquipmentFaultDetectedEvent(String equipmentId, String reason) {
        this.equipmentId = equipmentId;
        this.reason = reason;
    }

    public String getEquipmentId() { return equipmentId; }
    public String getReason() { return reason; }
}

