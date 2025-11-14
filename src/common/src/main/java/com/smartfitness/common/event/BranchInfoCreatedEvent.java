package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: 신규 지점 정보가 등록됨
 * UC-18: Branch Registration
 * Published by: BranchOwner Service
 * Consumed by: PreferenceMatchConsumer (Search Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchInfoCreatedEvent implements DomainEvent {
    private String eventId;
    private String branchId;
    private String branchName;
    private String address;
    private String facilityDescription;
    private Instant createdAt;
    
    @Override
    public String getEventType() {
        return "BranchInfoCreatedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return createdAt;
    }
    
    @Override
    public String getAggregateId() {
        return branchId;
    }
    
    public static BranchInfoCreatedEvent create(String branchId, String branchName, 
                                               String address, String facilityDescription) {
        return BranchInfoCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .branchId(branchId)
                .branchName(branchName)
                .address(address)
                .facilityDescription(facilityDescription)
                .createdAt(Instant.now())
                .build();
    }
}

