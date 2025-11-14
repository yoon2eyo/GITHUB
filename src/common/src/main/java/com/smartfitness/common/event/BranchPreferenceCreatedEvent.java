package com.smartfitness.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event: 신규 지점 선호도가 생성됨 (리뷰 또는 지점 정보 등록 시)
 * UC-10, UC-18, DD-06, DD-07: Cold Path Indexing & Scheduled Matching
 * Published by: Search Service (ContentRegistrationManager)
 * Consumed by: PreferenceMatchConsumer (Search Service)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchPreferenceCreatedEvent implements DomainEvent {
    private String eventId;
    private String branchId;
    private List<String> extractedKeywords; // LLM 추출 키워드
    private String preferenceCategory; // "헬스", "크로스핏", "필라테스" 등
    private Instant createdAt;
    
    @Override
    public String getEventType() {
        return "BranchPreferenceCreatedEvent";
    }
    
    @Override
    public Instant getOccurredAt() {
        return createdAt;
    }
    
    @Override
    public String getAggregateId() {
        return branchId;
    }
    
    public static BranchPreferenceCreatedEvent create(String branchId, 
                                                     List<String> extractedKeywords, 
                                                     String preferenceCategory) {
        return BranchPreferenceCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .branchId(branchId)
                .extractedKeywords(extractedKeywords)
                .preferenceCategory(preferenceCategory)
                .createdAt(Instant.now())
                .build();
    }
}

