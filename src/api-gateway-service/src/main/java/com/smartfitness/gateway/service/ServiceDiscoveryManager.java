package com.smartfitness.gateway.service;

import com.smartfitness.gateway.adapter.IServiceRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Business Layer: Service Discovery Manager
 * Component: ServiceDiscoveryManager
 * Interacts with Eureka Service Registry
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceDiscoveryManager implements IServiceDiscoveryService {
    
    private final IServiceRegistry serviceRegistry;
    
    @Override
    public String discoverService(String serviceName) {
        log.info("Discovering service: {}", serviceName);
        return serviceRegistry.getServiceUrl(serviceName);
    }
    
    @Override
    public List<String> getAllRegisteredServices() {
        log.info("Fetching all registered services");
        return serviceRegistry.getAllServices();
    }
    
    @Override
    public List<Map<String, Object>> getServiceInstances(String serviceName) {
        log.info("Fetching instances for service: {}", serviceName);
        return serviceRegistry.getInstances(serviceName);
    }
}

