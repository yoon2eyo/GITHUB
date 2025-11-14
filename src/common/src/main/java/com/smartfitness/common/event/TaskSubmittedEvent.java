package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Event: Helper가 세탁 작업 사진을 제출함
 * UC-12, UC-13: Helper Task Registration & AI Analysis
 * Published by: Helper Service
 * Consumed by: AITaskAnalysisConsumer (Helper Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSubmittedEvent implements DomainEvent {
    private String eventId;
    private String taskId;
    private String helperId;
    private String branchId;
    private String photoUrl;
    private Instant submittedAt;
    
    @Override
    public String getEventType() {
        return "TaskSubmittedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return submittedAt;
    }
    
    @Override
    public String getAggregateId() {
        return taskId;
    }
    
    public static TaskSubmittedEvent create(String taskId, String helperId, String branchId, String photoUrl) {
        return TaskSubmittedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .taskId(taskId)
                .helperId(helperId)
                .branchId(branchId)
                .photoUrl(photoUrl)
                .submittedAt(Instant.now())
                .build();
    }
}

