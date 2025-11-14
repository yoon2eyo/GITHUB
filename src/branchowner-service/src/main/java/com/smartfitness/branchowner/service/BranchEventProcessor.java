package com.smartfitness.branchowner.service;

import com.smartfitness.branchowner.adapter.IMessageSubscriptionService;
import com.smartfitness.branchowner.repository.IBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Branch Event Processor
 * Component: BranchEventProcessor
 * 
 * Processes branch-related events
 * - Subscribes to events via IMessageSubscriptionService
 * - Updates branch data via IBranchRepository
 * - Uses IBranchOwnerManagementService for business logic
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BranchEventProcessor {
    
    private final IBranchOwnerManagementService branchOwnerManagementService;
    private final IBranchRepository branchRepository;
    private final IMessageSubscriptionService messageSubscriptionService;
    
    /**
     * Subscribe to branch-related events
     */
    public void subscribeToBranchEvents() {
        log.info("Subscribing to branch events");
        messageSubscriptionService.subscribe("BranchInfoCreatedEvent", this);
        messageSubscriptionService.subscribe("BranchPreferenceCreatedEvent", this);
    }
    
    /**
     * Handle BranchInfoCreatedEvent
     */
    public void handleBranchInfoCreatedEvent(String branchId, String branchName) {
        log.info("Handling BranchInfoCreatedEvent: branchId={}, name={}", branchId, branchName);
        
        // Stub: Update branch info
        // branchRepository.updateBranchInfo(branchId, branchName);
    }
    
    /**
     * Handle BranchPreferenceCreatedEvent
     */
    public void handleBranchPreferenceCreatedEvent(String branchId, String preferenceDetails) {
        log.info("Handling BranchPreferenceCreatedEvent: branchId={}", branchId);
        
        // Stub: Store preference details
        // branchRepository.updatePreferences(branchId, preferenceDetails);
    }
}

