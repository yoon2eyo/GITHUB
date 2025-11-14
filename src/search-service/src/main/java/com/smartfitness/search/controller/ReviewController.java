package com.smartfitness.search.controller;

import com.smartfitness.search.service.IContentRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Review Controller
 * Component: ReviewController
 * 
 * UC-10: Review Registration (Cold Path)
 * UC-18: Branch Info Registration (Cold Path)
 * 
 * Cold Path: Async indexing with LLM analysis
 * - Publishes BranchPreferenceCreatedEvent
 * - LLM extracts keywords/preferences
 * - Index to SearchEngine
 * 
 * Reference: 03_BranchContentServiceComponent.puml (IBranchReviewApi)
 */
@Slf4j
@RestController
@RequestMapping("/search/content")
@RequiredArgsConstructor
public class ReviewController implements IBranchReviewApi {
    
    private final IContentRegistrationService contentRegistrationService;
    
    /**
     * UC-10: Register customer review (Cold Path)
     * Triggers async indexing with LLM analysis
     */
    @Override
    @PostMapping("/reviews")
    public ResponseEntity<Map<String, Object>> registerReview(
            @RequestParam String branchId,
            @RequestParam String customerId,
            @RequestParam String review) {
        
        log.info("Review registration: branchId={}, customerId={}", branchId, customerId);
        
        Map<String, Object> result = contentRegistrationService.registerReview(branchId, customerId, review);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * UC-18: Register branch info (Cold Path)
     * Triggers async indexing with LLM analysis
     */
    @Override
    @PostMapping("/branches")
    public ResponseEntity<Map<String, Object>> registerBranchInfo(
            @RequestParam String branchOwnerId,
            @RequestBody Map<String, String> branchInfo) {
        
        log.info("Branch info registration: owner={}", branchOwnerId);
        
        Map<String, Object> result = contentRegistrationService.registerBranchInfo(branchOwnerId, branchInfo);
        
        return ResponseEntity.ok(result);
    }
}

