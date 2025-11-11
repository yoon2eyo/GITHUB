package com.smartfitness.facemodel.ports;

/**
 * 얼굴 벡터 유사도 계산 인터페이스
 * 실시간 벡터 비교 연산
 */
public interface IFaceModelService {
    /**
     * 두 개의 얼굴 벡터 간 유사도 계산
     * @param requestedVector 요청된 벡터
     * @param storedVector 저장된 벡터
     * @return 코사인 유사도 점수 (0.0 ~ 1.0)
     */
    double calculateSimilarityScore(byte[] requestedVector, byte[] storedVector);

    /**
     * 임계값 기반 인증 검증
     * @param similarityScore 유사도 점수
     * @return 인증 통과 여부
     */
    boolean isAuthenticated(double similarityScore);

    /**
     * 모델 상태 확인
     * @return 모델 로드 상태
     */
    boolean isModelLoaded();
}
