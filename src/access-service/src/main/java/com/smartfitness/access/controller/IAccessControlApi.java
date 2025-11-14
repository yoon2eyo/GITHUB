package com.smartfitness.access.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: Access Control API Interface
 * Reference: 10_RealTimeAccessServiceComponent.puml (IAccessControlApi)
 */
public interface IAccessControlApi {
    ResponseEntity<Map<String, Object>> requestFaceAccess(String branchId, String equipmentId, MultipartFile facePhoto);
    ResponseEntity<Map<String, String>> controlGate(String equipmentId, String action);
}

