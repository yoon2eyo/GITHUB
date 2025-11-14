package com.smartfitness.gateway.controller;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: API Gateway Management Interface
 * Reference: 07_ApiGatewayComponent.puml (IApiGatewayManagement)
 */
public interface IApiGatewayManagement {
    ResponseEntity<Map<String, String>> getHealth();
    ResponseEntity<List<String>> getRegisteredServices();
    ResponseEntity<List<Map<String, Object>>> getServiceInstances(String serviceName);
    ResponseEntity<Map<String, String>> getCircuitBreakerStatus();
    ResponseEntity<Map<String, Object>> getRateLimiterStats();
}

