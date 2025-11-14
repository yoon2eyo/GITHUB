package com.smartfitness.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

/**
 * Interface Layer: API Gateway Entry Interface
 * Reference: 07_ApiGatewayComponent.puml (IApiGatewayEntry)
 */
public interface IApiGatewayEntry {
    ResponseEntity<?> routeRequest(HttpServletRequest request, Object body);
}

