package com.smartfitness.access.service;

import com.smartfitness.access.adapter.IFaceModelServiceClient;
import com.smartfitness.access.cache.FaceVectorCache;
import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;
import com.smartfitness.common.event.AccessDeniedEvent;
import com.smartfitness.common.event.AccessGrantedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Business Layer: Access Authorization Manager
 * Component: AccessAuthorizationManager
 * 
 * DD-05 Pipeline Optimization:
 * 1. Receive face photo from Equipment
 * 2. Check FaceVectorCache (Data Pre-Fetching)
 * 3. IPC call to FaceModel Service (Same Physical Node)
 * 4. Vector matching (parallel stage in FaceModel)
 * 5. Gate control decision
 * 
 * QAS-02: 95% within 3 seconds
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccessAuthorizationManager implements IAccessAuthorizationService {
    
    private final FaceVectorCache faceVectorCache;
    private final IGateControlService gateControlService;
    private final IFaceModelServiceClient faceModelServiceClient;
    private final IAccessEventPublisher accessEventPublisher;
    
    private static final double SIMILARITY_THRESHOLD = 0.85;
    
    @Override
    public Map<String, Object> authorizeFaceAccess(String branchId, String equipmentId, MultipartFile facePhoto) {
        log.info("Authorizing face access: branch={}, equipment={}", branchId, equipmentId);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Extract features from uploaded photo (stub)
            byte[] photoBytes = facePhoto.getBytes();
            log.debug("Face photo size: {} bytes", photoBytes.length);
            
            // Step 2: Get active face vectors from cache (DD-05: Data Pre-Fetching)
            // Cache hit rate: >90% (removes DB I/O from hot path)
            FaceVectorDto cachedVector = faceVectorCache.getActiveVector(branchId);
            if (cachedVector == null) {
                log.warn("No active vectors found in cache for branch: {}", branchId);
                return createDeniedResponse(branchId, equipmentId, "NO_ACTIVE_USERS", 0.0, startTime);
            }
            
            log.debug("Cache hit for branch {}: {} active vectors", branchId, 1); // Stub: 1 vector
            
            // Step 3: IPC call to FaceModel Service (DD-05: Same Physical Node)
            // IPC/gRPC for minimum latency (~205ms with pipeline optimization)
            SimilarityResultDto similarity = faceModelServiceClient.calculateSimilarity(
                    photoBytes,
                    cachedVector
            );
            
            log.info("Similarity score: {} (threshold: {})", similarity.getSimilarityScore(), SIMILARITY_THRESHOLD);
            
            // Step 4: Access decision
            if (similarity.getIsMatch() && similarity.getSimilarityScore() >= SIMILARITY_THRESHOLD) {
                // GRANTED
                String userId = similarity.getUserId();
                log.info("Access GRANTED for user: {}", userId);
                
                // Step 5: Open gate
                gateControlService.openGate(equipmentId);
                
                // Step 6: Publish event
                accessEventPublisher.publishAccessGranted(
                        AccessGrantedEvent.create(userId, branchId, equipmentId, similarity.getSimilarityScore())
                );
                
                return Map.of(
                        "result", "GRANTED",
                        "userId", userId,
                        "similarityScore", similarity.getSimilarityScore(),
                        "processingTimeMs", System.currentTimeMillis() - startTime
                );
                
            } else {
                // DENIED
                log.warn("Access DENIED: similarity {} below threshold {}", 
                        similarity.getSimilarityScore(), SIMILARITY_THRESHOLD);
                return createDeniedResponse(
                        branchId, 
                        equipmentId, 
                        "NO_MATCH", 
                        similarity.getSimilarityScore(), 
                        startTime
                );
            }
            
        } catch (Exception e) {
            log.error("Face access authorization failed", e);
            return createDeniedResponse(branchId, equipmentId, "SYSTEM_ERROR", 0.0, startTime);
        }
    }
    
    @Override
    public Map<String, Object> authorizeQRAccess(String branchId, String equipmentId, String qrCode) {
        log.info("Authorizing QR access: branch={}, equipment={}, qr={}", branchId, equipmentId, qrCode);
        
        // Stub: QR code validation
        // In production: Validate QR code, check membership, etc.
        
        boolean isValid = qrCode != null && qrCode.startsWith("QR-");
        
        if (isValid) {
            String userId = extractUserIdFromQR(qrCode);
            gateControlService.openGate(equipmentId);
            
            accessEventPublisher.publishAccessGranted(
                    AccessGrantedEvent.create(userId, branchId, equipmentId, 1.0)
            );
            
            return Map.of(
                    "result", "GRANTED",
                    "userId", userId,
                    "method", "QR"
            );
        } else {
            accessEventPublisher.publishAccessDenied(
                    AccessDeniedEvent.create(branchId, equipmentId, "INVALID_QR", 0.0)
            );
            
            return Map.of(
                    "result", "DENIED",
                    "reason", "INVALID_QR"
            );
        }
    }
    
    @Override
    public boolean controlGate(String equipmentId, String action) {
        log.info("Manual gate control: equipment={}, action={}", equipmentId, action);
        
        if ("OPEN".equals(action)) {
            return gateControlService.openGate(equipmentId);
        } else if ("CLOSE".equals(action)) {
            return gateControlService.closeGate(equipmentId);
        }
        
        return false;
    }
    
    private Map<String, Object> createDeniedResponse(String branchId, String equipmentId, 
                                                     String reason, Double score, long startTime) {
        accessEventPublisher.publishAccessDenied(
                AccessDeniedEvent.create(branchId, equipmentId, reason, score)
        );
        
        return Map.of(
                "result", "DENIED",
                "reason", reason,
                "similarityScore", score,
                "processingTimeMs", System.currentTimeMillis() - startTime
        );
    }
    
    private String extractUserIdFromQR(String qrCode) {
        // Stub: Extract user ID from QR code
        return qrCode.replace("QR-", "USER-");
    }
}

