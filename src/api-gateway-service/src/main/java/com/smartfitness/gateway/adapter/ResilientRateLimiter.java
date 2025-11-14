package com.smartfitness.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: Resilient Rate Limiter
 * Component: ResilientRateLimiter
 * Uses Resilience4j for rate limiting
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Component
public class ResilientRateLimiter implements IRateLimiter {
    
    private static final int MAX_REQUESTS_PER_SECOND = 100;
    private int currentRequests = 0;
    
    @Override
    public boolean allowRequest() {
        // Stub: Simple counter-based rate limiting
        // In production: Use Resilience4j RateLimiter or Redis-based distributed rate limiting
        
        currentRequests++;
        
        if (currentRequests > MAX_REQUESTS_PER_SECOND) {
            log.warn("Rate limit exceeded: {} requests", currentRequests);
            currentRequests = 0; // Reset for next window
            return false;
        }
        
        log.debug("Request allowed: {}/{}", currentRequests, MAX_REQUESTS_PER_SECOND);
        return true;
    }
}

