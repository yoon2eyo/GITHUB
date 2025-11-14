package com.smartfitness.auth.service;

/**
 * Business Layer Interface: Authorization Service
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface IAuthorizationService {
    boolean checkPermission(String uri, String method);
    boolean hasRole(String userId, String role);
}

