package com.smartfitness.auth.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Business Layer Interface: User Registration Service
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface IUserRegistrationService {
    String registerCustomer(String email, String password, String name, String phone, String creditCardNumber);
    String registerBranchOwner(String email, String password, String name, String phone, String businessNumber);
    boolean registerFaceVector(String userId, MultipartFile facePhoto);
}

