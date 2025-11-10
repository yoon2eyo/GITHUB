package com.smartfitness.domain.model;

/**
 * UserAccount: Domain aggregate for a user.
 */
public class UserAccount {
    private final String userId;
    private final String username;

    public UserAccount(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
}

