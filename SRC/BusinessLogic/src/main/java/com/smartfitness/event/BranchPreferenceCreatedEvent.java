package com.smartfitness.event;

import java.util.List;

/**
 * BranchPreferenceCreatedEvent: Emitted when branch preference keywords are extracted.
 * 
 * DD-06 Usage: Published when registerContent() extracts keywords via LLM.
 * Consumed by: PreferenceMatchConsumer for async batch processing (Cold Path).
 */
public class BranchPreferenceCreatedEvent implements IDomainEvent {
    private final Long branchId;
    private final List<String> keywords;

    public BranchPreferenceCreatedEvent(Long branchId, List<String> keywords) {
        this.branchId = branchId;
        this.keywords = keywords;
    }

    public Long getBranchId() { return branchId; }
    public List<String> getKeywords() { return keywords; }
}
