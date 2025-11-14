package com.smartfitness.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: Authorization Client Adapter
 * Component: AuthorizationClientAdapter
 * Communicates with Auth Service via gRPC
 * DD-08: Role-based Authorization
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Component
public class AuthorizationClientAdapter implements IAuthorizationClient {
    
    @Override
    public boolean checkPermission(String uri, String method) {
        log.debug("Checking permission via gRPC call to Auth Service: {} {}", method, uri);
        
        // Stub: In production, make gRPC call to auth-service
        // Example: authServiceStub.checkPermission(CheckPermissionRequest.newBuilder()
        //          .setUri(uri).setMethod(method).build())
        
        // For now, allow all requests (stub behavior)
        return true;
    }
}

