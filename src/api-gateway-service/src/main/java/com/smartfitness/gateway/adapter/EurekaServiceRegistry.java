package com.smartfitness.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * System Interface Layer: Eureka Service Registry Adapter
 * Component: EurekaServiceRegistry
 * Integrates with Netflix Eureka for service discovery
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Component
public class EurekaServiceRegistry implements IServiceRegistry {
    
    @Override
    public String getServiceUrl(String serviceName) {
        log.info("Fetching service URL from Eureka: {}", serviceName);
        
        // Stub: In production, query Eureka registry
        return "http://localhost:8080/" + serviceName;
    }
    
    @Override
    public List<String> getAllServices() {
        log.info("Fetching all services from Eureka");
        
        // Stub: Return mock service list
        return List.of(
                "auth-service",
                "access-service",
                "search-service",
                "helper-service",
                "branchowner-service",
                "facemodel-service",
                "monitoring-service",
                "notification-service",
                "mlops-service"
        );
    }
    
    @Override
    public List<Map<String, Object>> getInstances(String serviceName) {
        log.info("Fetching instances for service: {}", serviceName);
        
        // Stub: Return mock instance list
        return List.of(
                Map.of(
                        "instanceId", serviceName + "-1",
                        "host", "localhost",
                        "port", 8080,
                        "status", "UP"
                ),
                Map.of(
                        "instanceId", serviceName + "-2",
                        "host", "localhost",
                        "port", 8081,
                        "status", "UP"
                )
        );
    }
}

