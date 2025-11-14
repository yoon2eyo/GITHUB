package com.smartfitness.gateway.adapter;

import java.util.List;
import java.util.Map;

/**
 * System Interface Layer: Service Registry Interface
 * Adapter for Eureka Service Discovery
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IServiceRegistry {
    String getServiceUrl(String serviceName);
    List<String> getAllServices();
    List<Map<String, Object>> getInstances(String serviceName);
}

