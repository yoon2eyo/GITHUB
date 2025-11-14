package com.smartfitness.branchowner.controller;

import com.smartfitness.branchowner.service.IBranchOwnerManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Branch Owner Controller
 * Component: BranchOwnerController
 * 
 * UC-03: Branch Owner Account Registration
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml (IBranchOwnerApi)
 */
@Slf4j
@RestController
@RequestMapping("/branchowner")
@RequiredArgsConstructor
public class BranchOwnerController implements IBranchOwnerApi {
    
    private final IBranchOwnerManagementService branchOwnerManagementService;
    
    /**
     * UC-03: Register branch owner account
     */
    @Override
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerBranchOwner(@RequestBody Map<String, String> ownerInfo) {
        log.info("Branch owner registration request: {}", ownerInfo.get("email"));
        
        Map<String, Object> result = branchOwnerManagementService.registerBranchOwner(ownerInfo);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get branch owner information
     */
    @Override
    @GetMapping("/{ownerId}")
    public ResponseEntity<Map<String, Object>> getBranchOwnerInfo(@PathVariable String ownerId) {
        log.info("Get branch owner info: {}", ownerId);
        
        // Stub: Return owner info
        return ResponseEntity.ok(Map.of(
                "ownerId", ownerId,
                "email", "owner@example.com",
                "name", "Owner Name"
        ));
    }
}

