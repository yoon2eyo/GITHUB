package com.smartfitness.auth.internal.logic;

import com.smartfitness.auth.model.AuthToken;

import com.smartfitness.domain.model.UserAccount;

/**
 * TokenService: Encapsulates token issuing/validation and password verification.
 */
public class TokenService {
    public static boolean validate(String token) {
        return token != null && !token.isBlank();
    }

    public static boolean verifyPassword(String rawPassword, String storedHash) {
        // Placeholder: compare hashed values; DO NOT use plain text in real code
        return storedHash != null && storedHash.equals("hash:" + rawPassword);
    }

    public static AuthToken issueToken(UserAccount account) {
        // Placeholder: sign and include claims
        return new AuthToken("token-for-" + account.getUserId());
    }
}

