package com.smartfitness.access.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Business Layer Interface: Access Authorization Service
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IAccessAuthorizationService {
    Map<String, Object> authorizeFaceAccess(String branchId, String equipmentId, MultipartFile facePhoto);
    Map<String, Object> authorizeQRAccess(String branchId, String equipmentId, String qrCode);
    boolean controlGate(String equipmentId, String action);
}

