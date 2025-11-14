package com.smartfitness.auth.controller;

import com.smartfitness.auth.service.IUserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: User Management Controller
 * Component: UserManagementController
 * Endpoints: User Registration, Face Registration
 * Reference: 02_AuthenticationServiceComponent.puml (IAuthManagementApi)
 */
@Slf4j
@RestController
@RequestMapping("/auth/users")
@RequiredArgsConstructor
public class UserManagementController implements IAuthManagementApi {
    
    private final IUserRegistrationService userRegistrationService;
    
    /**
     * UC-01: Customer registration (email + credit card)
     */
    @PostMapping("/register/customer")
    public ResponseEntity<Map<String, Object>> registerCustomer(@RequestBody Map<String, String> userInfo) {
        log.info("Customer registration request for email: {}", userInfo.get("email"));
        
        String userId = userRegistrationService.registerCustomer(
                userInfo.get("email"),
                userInfo.get("password"),
                userInfo.get("name"),
                userInfo.get("phone"),
                userInfo.get("creditCardNumber")
        );
        
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Customer registered successfully"
        ));
    }
    
    /**
     * UC-02: Face vector registration
     */
    @PostMapping("/{userId}/face")
    public ResponseEntity<Map<String, String>> registerFace(
            @PathVariable String userId,
            @RequestParam("facePhoto") MultipartFile facePhoto
    ) {
        log.info("Face registration request for user: {}", userId);
        
        boolean success = userRegistrationService.registerFaceVector(userId, facePhoto);
        
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Face registered successfully"));
        } else {
            return ResponseEntity.status(400).body(Map.of("message", "Face registration failed"));
        }
    }
    
    /**
     * UC-03: Branch Owner registration
     */
    @PostMapping("/register/branch-owner")
    public ResponseEntity<Map<String, Object>> registerBranchOwner(@RequestBody Map<String, String> ownerInfo) {
        log.info("Branch Owner registration request for email: {}", ownerInfo.get("email"));
        
        String userId = userRegistrationService.registerBranchOwner(
                ownerInfo.get("email"),
                ownerInfo.get("password"),
                ownerInfo.get("name"),
                ownerInfo.get("phone"),
                ownerInfo.get("businessNumber")
        );
        
        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "message", "Branch Owner registered successfully"
        ));
    }
}

