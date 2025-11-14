package com.smartfitness.branchowner.controller;

import com.smartfitness.branchowner.service.IBranchInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: Branch Query Controller
 * Component: BranchQueryController
 * 
 * UC-18: Branch Info Registration
 * UC-19: Customer Review Inquiry
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchQueryApi)
 */
@Slf4j
@RestController
@RequestMapping("/branchowner/branches")
@RequiredArgsConstructor
public class BranchQueryController implements IBranchQueryApi {
    
    private final IBranchInfoService branchInfoService;
    
    /**
     * UC-18: Get branch information
     */
    @Override
    @GetMapping("/{branchId}")
    public ResponseEntity<Map<String, Object>> getBranchInfo(@PathVariable String branchId) {
        log.info("Get branch info: {}", branchId);
        
        Map<String, Object> branchInfo = branchInfoService.getBranchInfo(branchId);
        
        return ResponseEntity.ok(branchInfo);
    }
    
    /**
     * UC-19: Get customer reviews for branch
     */
    @Override
    @GetMapping("/{branchId}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getCustomerReviews(@PathVariable String branchId) {
        log.info("Get customer reviews for branch: {}", branchId);
        
        // Stub: Return reviews
        List<Map<String, Object>> reviews = List.of(
                Map.of("reviewId", "review-001", "customerId", "customer-001", "rating", 5),
                Map.of("reviewId", "review-002", "customerId", "customer-002", "rating", 4)
        );
        
        return ResponseEntity.ok(reviews);
    }
}

