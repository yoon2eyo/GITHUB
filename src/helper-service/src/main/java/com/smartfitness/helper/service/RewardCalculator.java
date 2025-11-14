package com.smartfitness.helper.service;

import com.smartfitness.helper.repository.IHelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Reward Calculator
 * Component: RewardCalculator
 * 
 * UC-16: Reward Balance Update
 * Calculates reward amount and updates helper's balance
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewardCalculator implements IRewardCalculationService {
    
    private final IHelperRepository helperRepository;
    
    private static final int REWARD_AMOUNT_PER_TASK = 1000; // KRW
    
    @Override
    public int calculateReward(String taskId) {
        log.debug("Calculating reward for task: {}", taskId);
        
        // Stub: Fixed amount for now
        return REWARD_AMOUNT_PER_TASK;
    }
    
    @Override
    public void updateRewardBalance(String helperId, int amount) {
        log.info("Updating reward balance for helper: {}, amount: {}", helperId, amount);
        
        // Stub: Update balance in database
        // helperRepository.incrementRewardBalance(helperId, amount);
        
        log.debug("Reward balance updated for helper: {}", helperId);
    }
}

