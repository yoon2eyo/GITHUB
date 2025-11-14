package com.smartfitness.auth.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: Auth Service API Interface
 * Reference: 02_AuthenticationServiceComponent.puml (IAuthServiceApi)
 */
public interface IAuthServiceApi {
    ResponseEntity<Map<String, String>> login(Map<String, String> credentials);
    ResponseEntity<Map<String, Boolean>> validateToken(String token);
    ResponseEntity<Map<String, Boolean>> checkPermission(String uri, String method);
}

