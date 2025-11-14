package com.smartfitness.auth.adapter;

/**
 * System Interface Layer: Token Service Interface
 * JWT token generation and validation
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface ITokenService {
    String generateToken(String userId, String role);
    boolean validateToken(String token);
    String extractUserId(String token);
}

