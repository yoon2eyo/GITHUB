package com.smartfitness.facemodel.interfaceadapter;

import com.smartfitness.facemodel.internal.VectorComparisonEngine;
import com.smartfitness.facemodel.internal.ModelLifecycleManager;
import com.smartfitness.facemodel.model.FaceVerificationResult;
import com.smartfitness.facemodel.ports.IFaceModelServiceApi;

/**
 * 얼굴 모델 서비스 API 구현 - 외부 클라이언트 인터페이스
 */
public class FaceModelServiceApiImpl implements IFaceModelServiceApi {
    private final VectorComparisonEngine vectorComparisonEngine;
    private final ModelLifecycleManager modelLifecycleManager;

    public FaceModelServiceApiImpl(VectorComparisonEngine vectorComparisonEngine,
                                   ModelLifecycleManager modelLifecycleManager) {
        this.vectorComparisonEngine = vectorComparisonEngine;
        this.modelLifecycleManager = modelLifecycleManager;
    }

    @Override
    public Double calculateSimilarityScore(byte[] faceVector1, byte[] faceVector2) {
        if (faceVector1 == null || faceVector2 == null) {
            throw new IllegalArgumentException("Face vectors cannot be null");
        }
        return vectorComparisonEngine.calculateSimilarityScore(faceVector1, faceVector2);
    }

    @Override
    public FaceVerificationResult verifyFace(String userId, byte[] faceVector) {
        if (userId == null || userId.isEmpty() || faceVector == null) {
            return new FaceVerificationResult(false, 0.0, "Invalid input parameters", 0L);
        }

        // 추후 구현: 저장된 얼굴 벡터와 비교
        // 현재는 스텁 구현
        long startTime = System.currentTimeMillis();
        Double similarityScore = 0.0; // 저장된 벡터와 비교 필요
        long processingTime = System.currentTimeMillis() - startTime;

        return vectorComparisonEngine.createVerificationResult(similarityScore, processingTime);
    }

    @Override
    public java.util.List<byte[]> getSavedFaceVectors(String userId) {
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }

        // 추후 구현: 저장된 얼굴 벡터 조회
        return new java.util.ArrayList<>();
    }

    @Override
    public void registerFaceVector(String userId, byte[] faceVector) {
        if (userId == null || userId.isEmpty() || faceVector == null) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        // 추후 구현: 얼굴 벡터 저장
    }
}
