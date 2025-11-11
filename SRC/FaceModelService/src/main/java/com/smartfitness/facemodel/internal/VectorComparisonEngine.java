package com.smartfitness.facemodel.internal;

import com.smartfitness.facemodel.model.FaceVerificationResult;
import java.util.Arrays;

/**
 * 벡터 비교 엔진 - 얼굴 벡터 유사도 계산
 */
public class VectorComparisonEngine {
    private static final Double SIMILARITY_THRESHOLD = 0.6;

    /**
     * 두 얼굴 벡터 간 유사도 계산 (코사인 유사도)
     */
    public Double calculateSimilarityScore(byte[] vector1, byte[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length == 0 || vector2.length == 0) {
            return 0.0;
        }

        // 바이트 배열을 double 배열로 변환
        double[] v1 = convertBytesToDoubles(vector1);
        double[] v2 = convertBytesToDoubles(vector2);

        if (v1.length != v2.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            magnitude1 += v1[i] * v1[i];
            magnitude2 += v2[i] * v2[i];
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * 유사도 점수로부터 인증 성공 여부 판단
     */
    public FaceVerificationResult createVerificationResult(Double similarityScore, Long processingTimeMs) {
        Boolean verified = similarityScore >= SIMILARITY_THRESHOLD;
        String reason = verified ? "Face matches stored vector" : "Face does not match stored vector";
        return new FaceVerificationResult(verified, similarityScore, reason, processingTimeMs);
    }

    /**
     * 모델이 정상적으로 로드되었는지 확인
     */
    public Boolean isModelLoaded(Object model) {
        return model != null;
    }

    /**
     * 바이트 배열을 double 배열로 변환
     */
    private double[] convertBytesToDoubles(byte[] bytes) {
        double[] doubles = new double[bytes.length / 8];
        for (int i = 0; i < doubles.length; i++) {
            long bits = 0;
            for (int j = 0; j < 8; j++) {
                bits = (bits << 8) | (bytes[i * 8 + j] & 0xFF);
            }
            doubles[i] = Double.longBitsToDouble(bits);
        }
        return doubles;
    }
}
