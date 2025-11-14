package com.smartfitness.gateway.adapter;

/**
 * System Interface Layer: Authentication Client Interface
 * Communicates with Auth Service via gRPC
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IAuthenticationClient {
    boolean validateToken(String token);
}

