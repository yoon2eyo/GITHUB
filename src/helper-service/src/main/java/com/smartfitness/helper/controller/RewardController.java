package com.smartfitness.helper.controller;

import com.smartfitness.helper.service.IRewardConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Reward Controller
 * Component: RewardController
 * 
 * UC-14: Reward Confirmation
 * UC-16: Reward Balance Update (Event-driven)
 * 
 * Reference: 04_HelperServiceComponent.puml (IHelperRewardApi)
 */
@Slf4j
@RestController
@RequestMapping("/helper/rewards")
@RequiredArgsConstructor
public class RewardController implements IHelperRewardApi {
    
    private final IRewardConfirmationService rewardConfirmationService;
    
    /**
     * UC-14: Get reward balance
     */
    @Override
    @GetMapping("/{helperId}/balance")
    public ResponseEntity<Map<String, Object>> getRewardBalance(@PathVariable String helperId) {
        log.info("Get reward balance for helper: {}", helperId);
        
        Map<String, Object> balance = rewardConfirmationService.getRewardBalance(helperId);
        
        return ResponseEntity.ok(balance);
    }
    
    /**
     * Get reward transaction history
     */
    @Override
    @GetMapping("/{helperId}/history")
    public ResponseEntity<Map<String, Object>> getRewardHistory(@PathVariable String helperId) {
        log.info("Get reward history for helper: {}", helperId);
        
        // Stub: Return reward history
        return ResponseEntity.ok(Map.of(
                "helperId", helperId,
                "history", Map.of(),
                "totalEarned", 0
        ));
    }
}

