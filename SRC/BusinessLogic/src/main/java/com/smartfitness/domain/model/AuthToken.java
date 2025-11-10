package com.smartfitness.domain.model;

/**
 * AuthToken: Represents an issued authentication token.
 */
public class AuthToken {
    private final String token;

    public AuthToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

