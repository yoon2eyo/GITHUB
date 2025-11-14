package com.smartfitness.branchowner.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * System Interface Layer: Auth JPA Repository
 * Component: AuthJpaRepository
 * 
 * Implements: IAuthRepository
 * Database: AuthDatabase (shared with Auth Service)
 * 
 * UC-03: Branch Owner Account Registration
 * - Creates authentication account for branch owner
 * 
 * Reference: 09_BranchOwnerServiceComponent.puml
 */
@Slf4j
@Repository
public class AuthJpaRepository implements IAuthRepository {
    
    @Override
    public void createAccount(String ownerId, Map<String, String> ownerInfo) {
        log.info("Creating auth account for branch owner: {}", ownerId);
        
        // Stub: In production, save to auth database
        // UserAccount account = new UserAccount();
        // account.setId(ownerId);
        // account.setEmail(ownerInfo.get("email"));
        // account.setPasswordHash(hashPassword(ownerInfo.get("password")));
        // account.setRole("BRANCH_OWNER");
        // authRepository.save(account);
    }
    
    @Override
    public Map<String, Object> findAccountById(String ownerId) {
        log.debug("Finding auth account: {}", ownerId);
        
        // Stub: In production, query auth database
        // UserAccount account = authRepository.findById(ownerId).orElseThrow();
        // return Map.of(
        //     "ownerId", account.getId(),
        //     "email", account.getEmail(),
        //     "role", account.getRole()
        // );
        
        return Map.of(
                "ownerId", ownerId,
                "email", "owner@example.com",
                "role", "BRANCH_OWNER"
        );
    }
}

