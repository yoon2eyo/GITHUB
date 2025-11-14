package com.smartfitness.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Enumeration;

/**
 * Business Layer: Request Router
 * Component: RequestRouter
 * Responsibilities:
 * - Security check (via SecurityManager)
 * - Service discovery (via ServiceDiscoveryManager)
 * - Load balancing (via LoadBalancer)
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestRouter implements IRequestRoutingService {
    
    private final ISecurityService securityService;
    private final IServiceDiscoveryService serviceDiscoveryService;
    private final ILoadBalancingService loadBalancingService;
    
    @Override
    public ResponseEntity<?> routeRequest(String method, String uri, Enumeration<String> headers, Object body) {
        log.info("Routing request: {} {}", method, uri);
        
        // Step 1: Security check
        boolean isAuthenticated = securityService.authenticate(headers);
        if (!isAuthenticated) {
            log.warn("Authentication failed for request: {}", uri);
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        boolean isAuthorized = securityService.authorize(uri, method);
        if (!isAuthorized) {
            log.warn("Authorization failed for request: {}", uri);
            return ResponseEntity.status(403).body("Forbidden");
        }
        
        // Step 2: Determine target service
        String targetService = determineTargetService(uri);
        log.info("Target service determined: {}", targetService);
        
        // Step 3: Service discovery
        String serviceUrl = serviceDiscoveryService.discoverService(targetService);
        
        // Step 4: Load balancing & forward
        ResponseEntity<?> response = loadBalancingService.forwardRequest(serviceUrl, method, uri, body);
        
        log.info("Request routed successfully to: {}", targetService);
        return response;
    }
    
    private String determineTargetService(String uri) {
        if (uri.startsWith("/auth")) return "auth-service";
        if (uri.startsWith("/search")) return "search-service";
        if (uri.startsWith("/helper")) return "helper-service";
        if (uri.startsWith("/branch")) return "branchowner-service";
        if (uri.startsWith("/access")) return "access-service";
        
        return "unknown-service";
    }
}

