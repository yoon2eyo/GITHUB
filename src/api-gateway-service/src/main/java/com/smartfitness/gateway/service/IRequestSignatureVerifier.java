package com.smartfitness.gateway.service;

/**
 * Business Layer Interface: Request Signature Verifier
 * DD-08: Verify Message Integrity
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface IRequestSignatureVerifier {
    boolean verify(String signature);
}

