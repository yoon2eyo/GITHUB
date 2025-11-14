package com.smartfitness.access.controller;

import com.smartfitness.access.service.IAccessAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Interface Layer: QR Access Controller
 * Component: QRAccessController
 * Alternative access method using QR code
 * Reference: 10_RealTimeAccessServiceComponent.puml (IQRAccessApi)
 */
@Slf4j
@RestController
@RequestMapping("/access/qr")
@RequiredArgsConstructor
public class QRAccessController implements IQRAccessApi {
    
    private final IAccessAuthorizationService accessAuthorizationService;
    
    /**
     * QR code access (alternative to face recognition)
     * Used when face recognition fails or as backup
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> requestQRAccess(
            @RequestParam String branchId,
            @RequestParam String equipmentId,
            @RequestParam String qrCode
    ) {
        log.info("QR access request: branch={}, equipment={}, qr={}", branchId, equipmentId, qrCode);
        
        Map<String, Object> result = accessAuthorizationService.authorizeQRAccess(
                branchId,
                equipmentId,
                qrCode
        );
        
        log.info("QR access processed: {}", result.get("result"));
        
        return ResponseEntity.ok(result);
    }
}

