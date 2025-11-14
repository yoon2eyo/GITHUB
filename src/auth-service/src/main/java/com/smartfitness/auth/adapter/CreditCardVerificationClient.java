package com.smartfitness.auth.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: Credit Card Verification Client
 * Component: CreditCardVerificationClient
 * Integrates with external payment gateway
 * UC-01: Customer Registration
 * Reference: 02_AuthenticationServiceComponent.puml, 00_Overall_Architecture.puml
 */
@Slf4j
@Component
public class CreditCardVerificationClient implements ICreditCardVerificationService {
    
    @Override
    public boolean verifyCard(String cardNumber) {
        log.info("Verifying credit card with external service");
        
        // Stub: In production, make HTTP call to payment gateway
        // Example: REST call to ICreditCardVerificationService (external partner)
        
        if (cardNumber == null || cardNumber.length() < 16) {
            log.warn("Invalid card number format");
            return false;
        }
        
        log.info("Credit card verification successful");
        return true; // Stub: Always return true
    }
}

