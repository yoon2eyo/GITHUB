package com.smartfitness.facemodel.controller;

import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;
import com.smartfitness.facemodel.service.IVectorComparisonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Interface Layer: FaceModel IPC Handler
 * Component: FaceModelIPCHandler
 * 
 * DD-05: IPC/gRPC endpoint for Access Service
 * (In production, this would be a gRPC service implementation)
 * For stub, using REST API as IPC simulation
 * 
 * Reference: 12_FaceModelServiceComponent.puml (IFaceModelServiceApi)
 */
@Slf4j
@RestController
@RequestMapping("/facemodel/ipc")
@RequiredArgsConstructor
public class FaceModelIPCHandler implements IFaceModelServiceApi {
    
    private final IVectorComparisonService vectorComparisonService;
    
    /**
     * IPC endpoint: Calculate similarity between requested photo and stored vector
     * DD-05: ~205ms with pipeline parallelization (49% improvement)
     * 
     * In production: This would be a gRPC method
     * rpc CalculateSimilarity(SimilarityRequest) returns (SimilarityResponse);
     */
    @PostMapping("/calculate-similarity")
    public SimilarityResultDto calculateSimilarity(
            @RequestBody SimilarityRequest request
    ) {
        log.info("IPC request: calculateSimilarity() for userId={}", request.getStoredVector().getUserId());
        
        long startTime = System.currentTimeMillis();
        
        // DD-05: Pipeline Optimization (CompletableFuture parallelization)
        SimilarityResultDto result = vectorComparisonService.calculateSimilarity(
                request.getRequestedPhoto(),
                request.getStoredVector()
        );
        
        long processingTime = System.currentTimeMillis() - startTime;
        log.info("IPC response: score={}, match={}, time={}ms", 
                result.getSimilarityScore(), result.getIsMatch(), processingTime);
        
        return result;
    }
    
    /**
     * IPC endpoint: Extract features from photo (called by Access Service)
     */
    @PostMapping("/extract-features")
    public FaceVectorDto extractFeatures(@RequestBody byte[] photoBytes) {
        log.info("IPC request: extractFeatures() for {} bytes", photoBytes.length);
        
        FaceVectorDto features = vectorComparisonService.extractFeatures(photoBytes);
        
        log.debug("Features extracted: {} dimensions", features.getDimension());
        return features;
    }
    
    /**
     * Request DTO for similarity calculation
     */
    public static class SimilarityRequest {
        private byte[] requestedPhoto;
        private FaceVectorDto storedVector;
        
        public byte[] getRequestedPhoto() {
            return requestedPhoto;
        }
        
        public void setRequestedPhoto(byte[] requestedPhoto) {
            this.requestedPhoto = requestedPhoto;
        }
        
        public FaceVectorDto getStoredVector() {
            return storedVector;
        }
        
        public void setStoredVector(FaceVectorDto storedVector) {
            this.storedVector = storedVector;
        }
    }
}

