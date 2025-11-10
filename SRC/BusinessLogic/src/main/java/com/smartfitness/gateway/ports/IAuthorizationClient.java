package com.smartfitness.gateway.ports;

/**
 * IAuthorizationClient: Optional contract for fine-grained authorization checks.
 */
public interface IAuthorizationClient {
    boolean checkAccess(String token, String resourcePath);
}

