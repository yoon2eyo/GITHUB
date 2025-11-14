package com.smartfitness.gateway.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: Authentication Client Adapter
 * Component: AuthenticationClientAdapter
 * Communicates with Auth Service via gRPC
 * DD-08: Token-based Authentication
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Component
public class AuthenticationClientAdapter implements IAuthenticationClient {
    
    @Override
    public boolean validateToken(String token) {
        log.debug("Validating token via gRPC call to Auth Service");
        
        // Stub: In production, make gRPC call to auth-service
        // Example: authServiceStub.validateToken(ValidateTokenRequest.newBuilder().setToken(token).build())
        
        return token != null && token.startsWith("Bearer");
    }
}

