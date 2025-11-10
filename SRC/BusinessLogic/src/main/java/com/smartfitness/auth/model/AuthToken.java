package com.smartfitness.auth.model;

/**
 * AuthToken: Token returned by Auth Service operations.
 */
public class AuthToken {
    private final String value;

    public AuthToken(String value) { this.value = value; }

    public String getValue() { return value; }
}

