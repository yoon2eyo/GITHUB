package com.smartfitness.auth.ports;

import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.UserCredentials;
import com.smartfitness.auth.model.RegistrationDetails;

/**
 * IAuthServiceApi: Facade for authentication and account management.
 */
public interface IAuthServiceApi {
    /**
     * Validate credentials and issue an authentication token (UC-04).
     */
    AuthToken login(UserCredentials credentials);

    /**
     * Validate supplied token for integrity and expiration.
     */
    boolean validateToken(String token);

    /**
     * Register a new user account (UC-01/02/03).
     */
    void registerUser(RegistrationDetails details);
}

