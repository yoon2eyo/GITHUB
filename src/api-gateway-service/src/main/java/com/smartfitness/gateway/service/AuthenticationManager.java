package com.smartfitness.gateway.service;

import com.smartfitness.gateway.adapter.IAuthenticationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Authentication Manager
 * Component: AuthenticationManager
 * Validates tokens via Auth Service (gRPC)
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationManager implements IAuthenticationService {
    
    private final IAuthenticationClient authenticationClient;
    
    @Override
    public boolean validateToken(String token) {
        log.debug("Validating token with Auth Service");
        return authenticationClient.validateToken(token);
    }
}

