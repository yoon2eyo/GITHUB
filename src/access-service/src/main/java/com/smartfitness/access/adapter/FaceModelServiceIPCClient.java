package com.smartfitness.access.adapter;

import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * System Interface Layer: FaceModel Service IPC Client
 * Component: FaceModelServiceIPCClient
 * 
 * DD-05 Tactic: Same Physical Node
 * - IPC/gRPC for minimum latency
 * - Shared memory optimization
 * - No network overhead
 * - calculateSimilarityScore() ~205ms (with pipeline optimization)
 * 
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
@Slf4j
@Component
public class FaceModelServiceIPCClient implements IFaceModelServiceClient {
    
    @Override
    public SimilarityResultDto calculateSimilarity(byte[] requestedPhoto, FaceVectorDto storedVector) {
        log.info("IPC call to FaceModel Service: calculateSimilarity()");
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Stub: In production, make gRPC call to FaceModel Service
            // Example:
            // FaceModelServiceGrpc.FaceModelServiceBlockingStub stub = ...
            // SimilarityRequest request = SimilarityRequest.newBuilder()
            //     .setRequestedPhoto(ByteString.copyFrom(requestedPhoto))
            //     .setStoredVector(...)
            //     .build();
            // SimilarityResponse response = stub.calculateSimilarity(request);
            
            // Simulate IPC call latency (DD-05: ~205ms with pipeline optimization)
            Thread.sleep(205);
            
            // Mock similarity calculation
            double similarityScore = Math.random();
            boolean isMatch = similarityScore > 0.85;
            String userId = isMatch ? storedVector.getUserId() : null;
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            log.info("FaceModel IPC response: score={}, match={}, time={}ms", 
                    similarityScore, isMatch, processingTime);
            
            if (isMatch) {
                return SimilarityResultDto.match(userId, similarityScore, processingTime, "v1.0.0");
            } else {
                return SimilarityResultDto.noMatch(similarityScore, processingTime, "v1.0.0");
            }
            
        } catch (InterruptedException e) {
            log.error("IPC call interrupted", e);
            Thread.currentThread().interrupt();
            return SimilarityResultDto.noMatch(0.0, System.currentTimeMillis() - startTime, "v1.0.0");
        }
    }
}

