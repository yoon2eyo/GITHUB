package com.smartfitness.auth.adapter;

/**
 * System Interface Layer: Credit Card Verification Service Interface
 * External service integration for payment verification
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface ICreditCardVerificationService {
    boolean verifyCard(String cardNumber);
}

