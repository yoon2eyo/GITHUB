package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: 출입 허용됨
 * UC-08: Face Recognition Access
 * Published by: Access Service
 * Consumed by: (Audit Logger, Analytics)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessGrantedEvent implements DomainEvent {
    private String eventId;
    private String userId;
    private String branchId;
    private String equipmentId;
    private Double similarityScore;
    private Instant accessedAt;
    
    @Override
    public String getEventType() {
        return "AccessGrantedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return accessedAt;
    }
    
    @Override
    public String getAggregateId() {
        return userId;
    }
    
    public static AccessGrantedEvent create(String userId, String branchId, 
                                           String equipmentId, Double similarityScore) {
        return AccessGrantedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .branchId(branchId)
                .equipmentId(equipmentId)
                .similarityScore(similarityScore)
                .accessedAt(Instant.now())
                .build();
    }
}

