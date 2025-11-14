package com.smartfitness.gateway.adapter;

import org.springframework.http.ResponseEntity;

import java.util.function.Supplier;

/**
 * System Interface Layer: Circuit Breaker Interface
 * Resilience4j Integration
 * DD-02: Availability Tactic
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface ICircuitBreaker {
    ResponseEntity<?> executeWithCircuitBreaker(Supplier<ResponseEntity<?>> operation);
}

