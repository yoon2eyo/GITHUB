package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: 출입 거부됨
 * UC-08: Face Recognition Access
 * Published by: Access Service
 * Consumed by: (Security Logger, Analytics)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDeniedEvent implements DomainEvent {
    private String eventId;
    private String branchId;
    private String equipmentId;
    private String denialReason; // "NO_MATCH", "EXPIRED_MEMBERSHIP", "BLOCKED_USER"
    private Double similarityScore;
    private Instant deniedAt;
    
    @Override
    public String getEventType() {
        return "AccessDeniedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return deniedAt;
    }
    
    @Override
    public String getAggregateId() {
        return equipmentId;
    }
    
    public static AccessDeniedEvent create(String branchId, String equipmentId, 
                                          String denialReason, Double similarityScore) {
        return AccessDeniedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .branchId(branchId)
                .equipmentId(equipmentId)
                .denialReason(denialReason)
                .similarityScore(similarityScore)
                .deniedAt(Instant.now())
                .build();
    }
}

