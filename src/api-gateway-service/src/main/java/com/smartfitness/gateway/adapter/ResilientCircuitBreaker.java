package com.smartfitness.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * System Interface Layer: Resilient Circuit Breaker
 * Component: ResilientCircuitBreaker
 * Uses Resilience4j for circuit breaking
 * DD-02: Availability - Escalating Restart
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Component
public class ResilientCircuitBreaker implements ICircuitBreaker {
    
    @Override
    public ResponseEntity<?> executeWithCircuitBreaker(Supplier<ResponseEntity<?>> operation) {
        log.debug("Executing operation with circuit breaker");
        
        try {
            // Stub: In production, wrap with @CircuitBreaker annotation or programmatic API
            // Example: circuitBreakerRegistry.circuitBreaker("gateway-cb").executeSupplier(operation)
            
            return operation.get();
            
        } catch (Exception e) {
            log.error("Circuit breaker fallback triggered", e);
            return ResponseEntity.status(503)
                    .body("Service temporarily unavailable. Please try again later.");
        }
    }
}

