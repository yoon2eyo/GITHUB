package com.smartfitness.access.controller;

import com.smartfitness.access.service.IAccessAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: Access Control Controller
 * Component: AccessControlController
 * UC-08: Face Recognition Access
 * QAS-02: 3초 이내 응답 (95%)
 * Reference: 10_RealTimeAccessServiceComponent.puml (IAccessControlApi)
 */
@Slf4j
@RestController
@RequestMapping("/access")
@RequiredArgsConstructor
public class AccessControlController implements IAccessControlApi {
    
    private final IAccessAuthorizationService accessAuthorizationService;
    
    /**
     * UC-08: Face recognition access
     * Equipment sends face photo for access authorization
     */
    @PostMapping("/face")
    public ResponseEntity<Map<String, Object>> requestFaceAccess(
            @RequestParam String branchId,
            @RequestParam String equipmentId,
            @RequestParam("facePhoto") MultipartFile facePhoto
    ) {
        long startTime = System.currentTimeMillis();
        log.info("Face access request: branch={}, equipment={}", branchId, equipmentId);
        
        // DD-05: Pipeline Optimization
        // AccessAuthorizationManager will:
        // 1. Check FaceVectorCache (Data Pre-Fetching)
        // 2. IPC call to FaceModel Service
        // 3. Gate control decision
        Map<String, Object> result = accessAuthorizationService.authorizeFaceAccess(
                branchId,
                equipmentId,
                facePhoto
        );
        
        long processingTime = System.currentTimeMillis() - startTime;
        result.put("processingTimeMs", processingTime);
        
        log.info("Face access processed in {}ms: {}", processingTime, result.get("result"));
        
        // QAS-02: Monitor 3-second SLA
        if (processingTime > 3000) {
            log.warn("QAS-02 VIOLATION: Processing time {}ms exceeded 3 seconds", processingTime);
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Manual gate control (emergency override)
     */
    @PostMapping("/gate/{equipmentId}/control")
    public ResponseEntity<Map<String, String>> controlGate(
            @PathVariable String equipmentId,
            @RequestParam String action // "OPEN" or "CLOSE"
    ) {
        log.info("Manual gate control: equipment={}, action={}", equipmentId, action);
        
        boolean success = accessAuthorizationService.controlGate(equipmentId, action);
        
        return ResponseEntity.ok(Map.of(
                "equipmentId", equipmentId,
                "action", action,
                "result", success ? "SUCCESS" : "FAILED"
        ));
    }
}

