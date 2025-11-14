package com.smartfitness.auth.controller;

import com.smartfitness.auth.service.IAuthenticationService;
import com.smartfitness.auth.service.IAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: Auth Service Controller
 * Component: AuthServiceController
 * Endpoints: Login, Logout, Token Validation
 * Reference: 02_AuthenticationServiceComponent.puml (IAuthServiceApi)
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthServiceController implements IAuthServiceApi {
    
    private final IAuthenticationService authenticationService;
    private final IAuthorizationService authorizationService;
    
    /**
     * UC-01: Customer login with email/password
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        log.info("Login request received for email: {}", credentials.get("email"));
        
        String token = authenticationService.authenticate(
                credentials.get("email"),
                credentials.get("password")
        );
        
        if (token != null) {
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "message", "Login successful"
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "message", "Invalid credentials"
            ));
        }
    }
    
    /**
     * Token validation (called by API Gateway)
     */
    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Boolean>> validateToken(@RequestHeader("Authorization") String token) {
        log.debug("Token validation request");
        
        boolean isValid = authenticationService.validateToken(token);
        
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
    
    /**
     * Check user permissions (called by API Gateway)
     */
    @PostMapping("/check-permission")
    public ResponseEntity<Map<String, Boolean>> checkPermission(
            @RequestParam String uri,
            @RequestParam String method
    ) {
        log.debug("Permission check: {} {}", method, uri);
        
        boolean hasPermission = authorizationService.checkPermission(uri, method);
        
        return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
    }
}

