package com.smartfitness.helper.service;

import com.smartfitness.helper.repository.IHelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Business Layer: Reward Confirmation Manager
 * Component: RewardConfirmationManager
 * 
 * UC-14: Reward Confirmation
 * Calculates and confirms helper's reward balance
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardConfirmationManager implements IRewardConfirmationService {
    
    private final IRewardCalculationService rewardCalculationService;
    private final IHelperRepository helperRepository;
    
    @Override
    public Map<String, Object> getRewardBalance(String helperId) {
        log.info("Getting reward balance for helper: {}", helperId);
        
        // Stub: Query helper's reward balance
        // int balance = helperRepository.getRewardBalance(helperId);
        int balance = 0; // Stub
        
        return Map.of(
                "helperId", helperId,
                "balance", balance,
                "currency", "KRW"
        );
    }
}

