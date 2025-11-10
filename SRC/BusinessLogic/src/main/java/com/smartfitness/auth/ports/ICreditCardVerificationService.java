package com.smartfitness.auth.ports;

/**
 * ICreditCardVerificationService: External credit card verification contract.
 */
public interface ICreditCardVerificationService {
    boolean verifyIdentity(String cardDetails, String userId);
}

