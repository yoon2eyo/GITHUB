package com.smartfitness.gateway.service;

/**
 * Business Layer Interface: Authorization Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IAuthorizationService {
    boolean checkPermission(String uri, String method);
}

