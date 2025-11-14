package com.smartfitness.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: Auth Management API Interface
 * Reference: 02_AuthenticationServiceComponent.puml (IAuthManagementApi)
 */
public interface IAuthManagementApi {
    ResponseEntity<Map<String, Object>> registerCustomer(Map<String, String> userInfo);
    ResponseEntity<Map<String, String>> registerFace(String userId, MultipartFile facePhoto);
    ResponseEntity<Map<String, Object>> registerBranchOwner(Map<String, String> ownerInfo);
}

