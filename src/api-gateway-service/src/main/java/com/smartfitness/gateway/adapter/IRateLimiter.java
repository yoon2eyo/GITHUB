package com.smartfitness.gateway.adapter;

/**
 * System Interface Layer: Rate Limiter Interface
 * Resilience4j Integration
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IRateLimiter {
    boolean allowRequest();
}

