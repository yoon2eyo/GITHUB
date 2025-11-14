package com.smartfitness.search.service;

import com.smartfitness.search.adapter.IMessageSubscriptionService;
import com.smartfitness.search.adapter.ISearchEngineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Business Layer: Preference Match Consumer
 * Component: PreferenceMatchConsumer
 * 
 * Consumes: BranchPreferenceCreatedEvent
 * 
 * **DD-07: Scheduling Policy**
 * - Defers matching during peak time (09:00-21:00)
 * - Processes in off-peak hours (21:00-09:00)
 * - Protects Hot Path performance
 * 
 * Flow:
 * 1. Subscribe to BranchPreferenceCreatedEvent
 * 2. Check if peak time → defer if yes
 * 3. If off-peak → process matching immediately
 * 4. Query SearchEngine for preference matching
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PreferenceMatchConsumer {
    
    private final IMessageSubscriptionService messageSubscriptionService;
    private final ISearchEngineRepository searchEngineRepository;
    
    private static final LocalTime PEAK_START = LocalTime.of(9, 0);
    private static final LocalTime PEAK_END = LocalTime.of(21, 0);
    
    /**
     * Subscribe to BranchPreferenceCreatedEvent
     */
    public void subscribeToBranchPreferenceCreated() {
        log.info("Subscribing to BranchPreferenceCreatedEvent");
        messageSubscriptionService.subscribe("BranchPreferenceCreatedEvent", this);
    }
    
    /**
     * Handle BranchPreferenceCreatedEvent
     * DD-07: Defer during peak time
     */
    public void handleBranchPreferenceCreatedEvent(String branchId, String preferenceDetails) {
        log.info("Handling BranchPreferenceCreatedEvent: branchId={}", branchId);
        
        // DD-07: Check peak time
        if (isPeakTime()) {
            log.info("Peak time detected, deferring preference matching for branch: {}", branchId);
            // Stub: Queue for later processing
            return;
        }
        
        // Off-peak time: Process immediately
        try {
            log.info("Off-peak time, processing preference matching for branch: {}", branchId);
            
            // Query SearchEngine for matching
            List<String> keywords = List.of(preferenceDetails.split(","));
            List<Map<String, Object>> matches = searchEngineRepository.search(keywords, null);
            
            log.info("Preference matching completed: {} matches found", matches.size());
            
        } catch (Exception e) {
            log.error("Failed to process preference matching for branch: {}", branchId, e);
        }
    }
    
    private boolean isPeakTime() {
        LocalTime now = LocalTime.now();
        return now.isAfter(PEAK_START) && now.isBefore(PEAK_END);
    }
}

