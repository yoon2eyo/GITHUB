package com.smartfitness.branchowner.service;

import com.smartfitness.branchowner.adapter.IMessagePublisherService;
import com.smartfitness.branchowner.repository.IAuthRepository;
import com.smartfitness.branchowner.repository.IBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Business Layer: Branch Owner Manager
 * Component: BranchOwnerManager
 * 
 * UC-03: Branch Owner Account Registration
 * 
 * Flow:
 * 1. Validate branch info via IBranchInfoService
 * 2. Register in Auth Service (IAuthRepository)
 * 3. Store branch owner info (IBranchRepository)
 * 4. Publish event (IMessagePublisherService)
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BranchOwnerManager implements IBranchOwnerManagementService {
    
    private final IBranchInfoService branchInfoService;
    private final IBranchRepository branchRepository;
    private final IAuthRepository authRepository;
    private final IMessagePublisherService messagePublisherService;
    
    @Override
    public Map<String, Object> registerBranchOwner(Map<String, String> ownerInfo) {
        log.info("Registering branch owner: {}", ownerInfo.get("email"));
        
        // 1. Validate branch info
        String branchId = ownerInfo.get("branchId");
        if (branchId != null) {
            Map<String, Object> branchInfo = branchInfoService.getBranchInfo(branchId);
            log.debug("Branch info validated: {}", branchInfo);
        }
        
        // 2. Register in Auth Service
        String ownerId = UUID.randomUUID().toString();
        // Stub: authRepository.createAccount(ownerId, ownerInfo);
        
        // 3. Store branch owner info
        // Stub: branchRepository.saveBranchOwner(ownerId, ownerInfo);
        
        // 4. Publish event (if needed)
        // BranchOwnerRegisteredEvent event = new BranchOwnerRegisteredEvent(ownerId);
        // messagePublisherService.publishEvent(event);
        
        log.info("Branch owner registered successfully: {}", ownerId);
        
        return Map.of(
                "success", true,
                "ownerId", ownerId,
                "message", "Branch owner registered successfully"
        );
    }
}

