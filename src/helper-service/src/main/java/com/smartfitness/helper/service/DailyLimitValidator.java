package com.smartfitness.helper.service;

import com.smartfitness.helper.repository.IHelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Daily Limit Validator
 * Component: DailyLimitValidator
 * 
 * UC-12: Validates helper's daily task limit (3 photos/day)
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyLimitValidator implements ITaskValidationService {
    
    private final IHelperRepository helperRepository;
    
    private static final int DAILY_LIMIT = 3;
    
    @Override
    public boolean validateDailyLimit(String helperId) {
        log.debug("Validating daily limit for helper: {}", helperId);
        
        // Stub: Query today's task count
        // int todayCount = helperRepository.countTodayTasks(helperId);
        int todayCount = 0; // Stub
        
        boolean isValid = todayCount < DAILY_LIMIT;
        
        if (!isValid) {
            log.warn("Daily limit exceeded for helper: {} (count: {})", helperId, todayCount);
        }
        
        return isValid;
    }
}

