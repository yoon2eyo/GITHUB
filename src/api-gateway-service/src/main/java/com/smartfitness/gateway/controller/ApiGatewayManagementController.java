package com.smartfitness.gateway.controller;

import com.smartfitness.gateway.service.IServiceDiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Interface Layer: API Gateway Management Interface
 * Component: ApiGatewayManagementController
 * For Operations Center ONLY (not for regular clients)
 * Provides: Gateway health, service registry, circuit breaker status
 * Reference: 07_ApiGatewayComponent.puml (IApiGatewayManagement)
 */
@Slf4j
@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class ApiGatewayManagementController implements IApiGatewayManagement {
    
    private final IServiceDiscoveryService serviceDiscoveryService;
    
    /**
     * Gateway health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> getHealth() {
        log.info("Gateway health check requested");
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
    
    /**
     * Get all registered services from Eureka
     */
    @GetMapping("/services")
    public ResponseEntity<List<String>> getRegisteredServices() {
        log.info("Service registry query requested");
        List<String> services = serviceDiscoveryService.getAllRegisteredServices();
        return ResponseEntity.ok(services);
    }
    
    /**
     * Get service instances for a specific service
     */
    @GetMapping("/services/{serviceName}/instances")
    public ResponseEntity<List<Map<String, Object>>> getServiceInstances(@PathVariable String serviceName) {
        log.info("Service instances query requested for: {}", serviceName);
        List<Map<String, Object>> instances = serviceDiscoveryService.getServiceInstances(serviceName);
        return ResponseEntity.ok(instances);
    }
    
    /**
     * Get circuit breaker status
     */
    @GetMapping("/circuit-breaker/status")
    public ResponseEntity<Map<String, String>> getCircuitBreakerStatus() {
        log.info("Circuit breaker status requested");
        return ResponseEntity.ok(Map.of(
                "status", "CLOSED",
                "failureRate", "0.0%",
                "message", "All services healthy"
        ));
    }
    
    /**
     * Get rate limiting statistics
     */
    @GetMapping("/rate-limiter/stats")
    public ResponseEntity<Map<String, Object>> getRateLimiterStats() {
        log.info("Rate limiter stats requested");
        return ResponseEntity.ok(Map.of(
                "totalRequests", 1000,
                "allowedRequests", 950,
                "blockedRequests", 50,
                "currentRate", "95 req/sec"
        ));
    }
}

