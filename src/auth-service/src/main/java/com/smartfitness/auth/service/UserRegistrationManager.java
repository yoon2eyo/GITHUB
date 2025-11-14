package com.smartfitness.auth.service;

import com.smartfitness.auth.adapter.ICreditCardVerificationService;
import com.smartfitness.auth.domain.User;
import com.smartfitness.auth.repository.IAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

/**
 * Business Layer: User Registration Manager
 * Component: UserRegistrationManager
 * Responsibilities:
 * - Customer registration (UC-01)
 * - Face vector registration (UC-02)
 * - Branch Owner registration (UC-03)
 * Reference: 02_AuthenticationServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationManager implements IUserRegistrationService {
    
    private final IAuthRepository authRepository;
    private final IAuthenticationService authenticationService;
    private final ICreditCardVerificationService creditCardVerificationService;
    
    @Override
    public String registerCustomer(String email, String password, String name, String phone, String creditCardNumber) {
        log.info("Registering customer: {}", email);
        
        // Step 1: Verify credit card (external service)
        boolean cardValid = creditCardVerificationService.verifyCard(creditCardNumber);
        if (!cardValid) {
            log.warn("Credit card verification failed for: {}", email);
            throw new RuntimeException("Invalid credit card");
        }
        
        // Step 2: Create user entity
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(hashPassword(password)) // Stub
                .role(User.UserRole.CUSTOMER)
                .name(name)
                .phoneNumber(phone)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
        
        // Step 3: Save to database
        authRepository.save(user);
        
        log.info("Customer registered successfully: {}", email);
        return user.getUserId();
    }
    
    @Override
    public String registerBranchOwner(String email, String password, String name, String phone, String businessNumber) {
        log.info("Registering branch owner: {}", email);
        
        // Step 1: Verify business number (stub)
        // In production: Call external business verification service
        
        // Step 2: Create user entity
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .email(email)
                .passwordHash(hashPassword(password))
                .role(User.UserRole.BRANCH_OWNER)
                .name(name)
                .phoneNumber(phone)
                .isActive(true)
                .createdAt(Instant.now())
                .build();
        
        // Step 3: Save to database
        authRepository.save(user);
        
        log.info("Branch owner registered successfully: {}", email);
        return user.getUserId();
    }
    
    @Override
    public boolean registerFaceVector(String userId, MultipartFile facePhoto) {
        log.info("Registering face vector for user: {}", userId);
        
        // Step 1: Find user
        User user = authRepository.findById(userId);
        if (user == null) {
            log.warn("User not found: {}", userId);
            return false;
        }
        
        // Step 2: Extract face vector (ML service call - stub)
        // In production: Call ML service to extract 512-dim vector
        String faceVector = extractFaceVector(facePhoto); // Stub
        
        // Step 3: Update user with face vector
        user.setFaceVectorData(faceVector);
        user.setFaceModelVersion("v1.0.0");
        authRepository.save(user);
        
        log.info("Face vector registered successfully for user: {}", userId);
        return true;
    }
    
    private String hashPassword(String password) {
        // Stub: In production, use BCrypt.hashpw(password, BCrypt.gensalt())
        return "hashed_" + password;
    }
    
    private String extractFaceVector(MultipartFile facePhoto) {
        // Stub: In production, call ML service for feature extraction
        return "[0.123, 0.456, ..., 0.789]"; // 512-dim vector
    }
}

