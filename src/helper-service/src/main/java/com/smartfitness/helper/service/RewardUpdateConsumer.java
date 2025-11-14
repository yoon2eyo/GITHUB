package com.smartfitness.helper.service;

import com.smartfitness.helper.adapter.IMessageSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Reward Update Consumer
 * Component: RewardUpdateConsumer
 * 
 * UC-16: Reward Balance Update (Event-Driven)
 * Consumes: TaskConfirmedEvent
 * 
 * Flow:
 * 1. BranchOwner confirms task as '양호' (Good)
 * 2. TaskConfirmedEvent published
 * 3. RewardUpdateConsumer handles event
 * 4. Update helper's reward balance
 * 
 * DD-02: Event-Based Architecture
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewardUpdateConsumer {
    
    private final IMessageSubscriptionService messageSubscriptionService;
    private final IRewardCalculationService rewardCalculationService;
    
    /**
     * Subscribe to TaskConfirmedEvent
     * In production, use @RabbitListener
     */
    public void subscribeToTaskConfirmedEvent() {
        log.info("Subscribing to TaskConfirmedEvent");
        messageSubscriptionService.subscribe("TaskConfirmedEvent", this);
    }
    
    /**
     * Handle TaskConfirmedEvent
     * Called when BranchOwner confirms task as '양호'
     */
    public void handleTaskConfirmedEvent(String taskId, String branchOwnerId, String status) {
        log.info("Handling TaskConfirmedEvent: taskId={}, status={}", taskId, status);
        
        if (!"GOOD".equalsIgnoreCase(status)) {
            log.debug("Task not confirmed as GOOD, skipping reward update");
            return;
        }
        
        try {
            // 1. Calculate reward
            int rewardAmount = rewardCalculationService.calculateReward(taskId);
            
            // 2. Update helper's balance
            // Stub: Get helperId from task
            String helperId = "helper-id"; // Stub
            rewardCalculationService.updateRewardBalance(helperId, rewardAmount);
            
            log.info("Reward updated for task {}: {} KRW", taskId, rewardAmount);
            
        } catch (Exception e) {
            log.error("Failed to update reward for task: {}", taskId, e);
        }
    }
}

