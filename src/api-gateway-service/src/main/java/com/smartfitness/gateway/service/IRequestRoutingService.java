package com.smartfitness.gateway.service;

import org.springframework.http.ResponseEntity;
import java.util.Enumeration;

/**
 * Business Layer Interface: Request Routing Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IRequestRoutingService {
    ResponseEntity<?> routeRequest(String method, String uri, Enumeration<String> headers, Object body);
}

