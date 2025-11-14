package com.smartfitness.auth.service;

import com.smartfitness.auth.adapter.ITokenService;
import com.smartfitness.auth.domain.User;
import com.smartfitness.auth.repository.IAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Business Layer: Authentication Manager
 * Component: AuthenticationManager
 * Responsibilities:
 * - User authentication (email/password)
 * - JWT token generation
 * - Token validation
 * DD-08: Token-based Authentication
 * Reference: 02_AuthenticationServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationManager implements IAuthenticationService {
    
    private final IAuthRepository authRepository;
    private final ITokenService tokenService;
    private final IAuthorizationService authorizationService;
    
    @Override
    public String authenticate(String email, String password) {
        log.info("Authenticating user: {}", email);
        
        // Step 1: Find user by email
        User user = authRepository.findByEmail(email);
        if (user == null) {
            log.warn("User not found: {}", email);
            return null;
        }
        
        // Step 2: Verify password (stub: simple comparison)
        if (!verifyPassword(password, user.getPasswordHash())) {
            log.warn("Invalid password for user: {}", email);
            return null;
        }
        
        // Step 3: Generate JWT token
        String token = tokenService.generateToken(user.getUserId(), user.getRole().name());
        
        log.info("Authentication successful for user: {}", email);
        return token;
    }
    
    @Override
    public boolean validateToken(String token) {
        log.debug("Validating token");
        
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // Remove "Bearer " prefix if present
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return tokenService.validateToken(token);
    }
    
    @Override
    public void logout(String token) {
        log.info("Logout request");
        // Stub: In production, invalidate token (blacklist in Redis)
    }
    
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        // Stub: In production, use BCrypt.checkpw(rawPassword, hashedPassword)
        return rawPassword != null && hashedPassword != null;
    }
}

