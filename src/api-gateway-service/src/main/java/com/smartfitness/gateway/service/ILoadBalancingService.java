package com.smartfitness.gateway.service;

import org.springframework.http.ResponseEntity;

/**
 * Business Layer Interface: Load Balancing Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface ILoadBalancingService {
    ResponseEntity<?> forwardRequest(String serviceUrl, String method, String uri, Object body);
}

