package com.smartfitness.gateway.ports;

import com.smartfitness.domain.model.AuthToken;

/**
 * IAuthenticationClient: Contract to communicate with Auth Service.
 */
public interface IAuthenticationClient {
    AuthToken authenticate(String userId, String password);
    boolean validateToken(String token);
}

