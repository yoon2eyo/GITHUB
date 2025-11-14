package com.smartfitness.gateway.service;

/**
 * Business Layer Interface: Authentication Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IAuthenticationService {
    boolean validateToken(String token);
}

