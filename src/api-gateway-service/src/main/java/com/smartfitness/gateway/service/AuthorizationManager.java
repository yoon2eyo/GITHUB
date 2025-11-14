package com.smartfitness.gateway.service;

import com.smartfitness.gateway.adapter.IAuthorizationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Authorization Manager
 * Component: AuthorizationManager
 * Checks permissions via Auth Service (gRPC)
 * Reference: 07_ApiGatewayComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationManager implements IAuthorizationService {
    
    private final IAuthorizationClient authorizationClient;
    
    @Override
    public boolean checkPermission(String uri, String method) {
        log.debug("Checking permission for: {} {}", method, uri);
        return authorizationClient.checkPermission(uri, method);
    }
}

