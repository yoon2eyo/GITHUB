package com.smartfitness.gateway.service;

import java.util.List;
import java.util.Map;

/**
 * Business Layer Interface: Service Discovery Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IServiceDiscoveryService {
    String discoverService(String serviceName);
    List<String> getAllRegisteredServices();
    List<Map<String, Object>> getServiceInstances(String serviceName);
}

