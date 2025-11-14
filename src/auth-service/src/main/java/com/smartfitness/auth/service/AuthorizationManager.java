package com.smartfitness.auth.service;

import com.smartfitness.auth.repository.IAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Authorization Manager
 * Component: AuthorizationManager
 * DD-08: Role-based Authorization
 * Reference: 02_AuthenticationServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationManager implements IAuthorizationService {
    
    private final IAuthRepository authRepository;
    
    @Override
    public boolean checkPermission(String uri, String method) {
        log.debug("Checking permission: {} {}", method, uri);
        
        // Stub: Simple role-based access control
        // In production: Implement fine-grained RBAC
        return true; // Allow all for stub
    }
    
    @Override
    public boolean hasRole(String userId, String role) {
        log.debug("Checking role for user {}: {}", userId, role);
        
        var user = authRepository.findById(userId);
        return user != null && user.getRole().name().equals(role);
    }
}

