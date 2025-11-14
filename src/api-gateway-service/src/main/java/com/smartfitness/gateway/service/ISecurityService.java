package com.smartfitness.gateway.service;

import java.util.Enumeration;

/**
 * Business Layer Interface: Security Service
 * Reference: 07_ApiGatewayComponent.puml
 */
public interface ISecurityService {
    boolean authenticate(Enumeration<String> headers);
    boolean authorize(String uri, String method);
    boolean verifySignature(String signature);
}

