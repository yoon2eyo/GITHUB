package com.smartfitness.facemodel.ports;

import com.smartfitness.facemodel.model.FaceVerificationResult;

/**
 * Face Model Service API 인터페이스
 * 외부 서비스(Access Service 등)에서 사용하는 인터페이스
 */
public interface IFaceModelServiceApi {
    /**
     * 얼굴 벡터 유사도 계산
     * @param requestedVector 요청된 얼굴 벡터
     * @param storedVector 저장된 얼굴 벡터
     * @return 유사도 점수 (0.0 ~ 1.0)
     */
    double calculateSimilarityScore(byte[] requestedVector, byte[] storedVector);

    /**
     * 얼굴 인증 검증
     * @param userId 사용자 ID
     * @param liveVector 라이브 얼굴 벡터
     * @return 인증 결과
     */
    FaceVerificationResult verifyFace(String userId, byte[] liveVector);

    /**
     * 저장된 모든 얼굴 벡터 조회
     * @param userId 사용자 ID
     * @return 얼굴 벡터 배열
     */
    byte[][] getSavedFaceVectors(String userId);

    /**
     * 새로운 얼굴 벡터 저장
     * @param userId 사용자 ID
     * @param faceVector 얼굴 벡터
     */
    void registerFaceVector(String userId, byte[] faceVector);
}
