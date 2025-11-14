package com.smartfitness.search.service;

import com.smartfitness.common.event.BranchInfoCreatedEvent;
import com.smartfitness.common.event.BranchPreferenceCreatedEvent;
import com.smartfitness.search.adapter.IMessagePublisherService;
import com.smartfitness.search.adapter.ISearchEngineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Business Layer: Content Registration Manager
 * Component: ContentRegistrationManager
 * 
 * UC-10: Review Registration (Cold Path)
 * UC-18: Branch Info Registration (Cold Path)
 * 
 * Flow (Cold Path):
 * 1. Review/BranchInfo created
 * 2. LLM keyword extraction (external) via IPreferenceAnalysisService
 * 3. Index to SearchEngine (DS-07)
 * 4. Publish BranchPreferenceCreatedEvent
 * 
 * DD-06: Cold Path allows external LLM calls
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentRegistrationManager implements IContentRegistrationService {
    
    private final IPreferenceAnalysisService preferenceAnalysisService;
    private final ISearchEngineRepository searchEngineRepository;
    private final IMessagePublisherService messagePublisherService;
    
    @Override
    public Map<String, Object> registerReview(String branchId, String customerId, String review) {
        log.info("Registering review: branchId={}, customerId={}", branchId, customerId);
        
        // 1. Analyze review with LLM (Cold Path - async)
        Map<String, Object> analysis = preferenceAnalysisService.analyzePreference(review);
        log.debug("Review analysis completed: {}", analysis);
        
        // 2. Index to SearchEngine
        String documentId = UUID.randomUUID().toString();
        Map<String, Object> document = Map.of(
                "documentId", documentId,
                "branchId", branchId,
                "customerId", customerId,
                "content", review,
                "keywords", analysis.getOrDefault("keywords", "")
        );
        searchEngineRepository.index(documentId, document);
        
        // 3. Publish BranchPreferenceCreatedEvent
        BranchPreferenceCreatedEvent event = new BranchPreferenceCreatedEvent(
                branchId, analysis.toString());
        messagePublisherService.publishEvent(event);
        
        log.info("Review registered successfully: {}", documentId);
        
        return Map.of(
                "success", true,
                "documentId", documentId,
                "message", "Review registered and indexed"
        );
    }
    
    @Override
    public Map<String, Object> registerBranchInfo(String branchOwnerId, Map<String, String> branchInfo) {
        log.info("Registering branch info: owner={}", branchOwnerId);
        
        String branchId = branchInfo.get("branchId");
        String content = branchInfo.get("description");
        
        // 1. Analyze with LLM (Cold Path - async)
        Map<String, Object> analysis = preferenceAnalysisService.analyzePreference(content);
        
        // 2. Index to SearchEngine
        String documentId = UUID.randomUUID().toString();
        Map<String, Object> document = Map.of(
                "documentId", documentId,
                "branchId", branchId,
                "branchOwnerId", branchOwnerId,
                "content", content,
                "keywords", analysis.getOrDefault("keywords", "")
        );
        searchEngineRepository.index(documentId, document);
        
        // 3. Publish BranchInfoCreatedEvent
        BranchInfoCreatedEvent event = new BranchInfoCreatedEvent(branchId, branchInfo.get("name"));
        messagePublisherService.publishEvent(event);
        
        log.info("Branch info registered successfully: {}", documentId);
        
        return Map.of(
                "success", true,
                "documentId", documentId,
                "message", "Branch info registered and indexed"
        );
    }
}

