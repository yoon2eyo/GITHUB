package com.smartfitness.gateway.service;

import com.smartfitness.gateway.adapter.ICircuitBreaker;
import com.smartfitness.gateway.adapter.IRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Load Balancer
 * Component: LoadBalancer
 * Responsibilities:
 * - Circuit breaker (Resilience4j)
 * - Rate limiting
 * - Request forwarding
 * DD-02: Availability Tactics
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoadBalancer implements ILoadBalancingService {
    
    private final ICircuitBreaker circuitBreaker;
    private final IRateLimiter rateLimiter;
    
    @Override
    public ResponseEntity<?> forwardRequest(String serviceUrl, String method, String uri, Object body) {
        log.info("Forwarding request to: {}", serviceUrl);
        
        // Step 1: Check rate limit
        if (!rateLimiter.allowRequest()) {
            log.warn("Rate limit exceeded");
            return ResponseEntity.status(429).body("Too Many Requests");
        }
        
        // Step 2: Circuit breaker
        return circuitBreaker.executeWithCircuitBreaker(() -> {
            log.debug("Executing request through circuit breaker");
            
            // Stub: Actual HTTP call to backend service
            return ResponseEntity.ok(Map.of(
                    "message", "Forwarded to " + serviceUrl,
                    "method", method,
                    "uri", uri
            ));
        });
    }
}

