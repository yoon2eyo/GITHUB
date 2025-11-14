package com.smartfitness.auth.service;

/**
 * Business Layer Interface: Authentication Service
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface IAuthenticationService {
    String authenticate(String email, String password);
    boolean validateToken(String token);
    void logout(String token);
}

