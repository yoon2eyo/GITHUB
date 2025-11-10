package com.smartfitness.auth.model;

import com.smartfitness.domain.model.UserAccount;

/**
 * RegistrationDetails: Data required to register a new user.
 */
public class RegistrationDetails {
    private final String userId;
    private final String username;
    private final String password;
    private final String cardDetails;

    public RegistrationDetails(String userId, String username, String password, String cardDetails) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.cardDetails = cardDetails;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getCardDetails() { return cardDetails; }

    public UserAccount toUserAccount() {
        return new UserAccount(userId, username);
    }
}

