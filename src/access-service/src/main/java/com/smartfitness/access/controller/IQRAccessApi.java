package com.smartfitness.access.controller;

import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Interface Layer: QR Access API Interface
 * Reference: 10_RealTimeAccessServiceComponent.puml (IQRAccessApi)
 */
public interface IQRAccessApi {
    ResponseEntity<Map<String, Object>> requestQRAccess(String branchId, String equipmentId, String qrCode);
}

