package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: 지점주가 작업을 확인함 (양호/미흡/불분명)
 * UC-15, UC-16: Task Confirmation & Reward Update
 * Published by: BranchOwner Service (또는 Helper Service 내부)
 * Consumed by: RewardUpdateConsumer (Helper Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskConfirmedEvent implements DomainEvent {
    private String eventId;
    private String taskId;
    private String branchId;
    private String branchOwnerId;
    private String confirmationStatus; // "양호", "미흡", "불분명"
    private Integer rewardAmount;
    private Instant confirmedAt;
    
    @Override
    public String getEventType() {
        return "TaskConfirmedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return confirmedAt;
    }
    
    @Override
    public String getAggregateId() {
        return taskId;
    }
    
    public static TaskConfirmedEvent create(String taskId, String branchId, String branchOwnerId, 
                                            String confirmationStatus, Integer rewardAmount) {
        return TaskConfirmedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .taskId(taskId)
                .branchId(branchId)
                .branchOwnerId(branchOwnerId)
                .confirmationStatus(confirmationStatus)
                .rewardAmount(rewardAmount)
                .confirmedAt(Instant.now())
                .build();
    }
}

