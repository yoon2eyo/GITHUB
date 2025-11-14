package com.smartfitness.gateway.adapter;

/**
 * System Interface Layer: Authorization Client Interface
 * Communicates with Auth Service via gRPC
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IAuthorizationClient {
    boolean checkPermission(String uri, String method);
}

