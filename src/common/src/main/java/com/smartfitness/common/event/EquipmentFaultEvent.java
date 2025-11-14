package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: 장비 고장 감지됨
 * UC-11, DD-04: Fault Detection (Heartbeat & Ping/echo)
 * QAS-01: 15초 이내 알림
 * Published by: Monitoring Service (FaultDetector)
 * Consumed by: NotificationDispatcherConsumer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentFaultEvent implements DomainEvent {
    private String eventId;
    private String equipmentId;
    private String branchId;
    private String equipmentType; // "CAMERA", "GATE", "SENSOR"
    private String faultType; // "HEARTBEAT_TIMEOUT", "PING_FAILED", "STATUS_ERROR"
    private String faultDescription;
    private Instant detectedAt;
    
    @Override
    public String getEventType() {
        return "EquipmentFaultEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return detectedAt;
    }
    
    @Override
    public String getAggregateId() {
        return equipmentId;
    }
    
    public static EquipmentFaultEvent create(String equipmentId, String branchId, 
                                            String equipmentType, String faultType, 
                                            String faultDescription) {
        return EquipmentFaultEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .equipmentId(equipmentId)
                .branchId(branchId)
                .equipmentType(equipmentType)
                .faultType(faultType)
                .faultDescription(faultDescription)
                .detectedAt(Instant.now())
                .build();
    }
}

