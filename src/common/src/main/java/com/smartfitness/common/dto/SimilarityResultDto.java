package com.smartfitness.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: Face Similarity Comparison Result
 * DD-05: IPC Response from FaceModel Service
 * QAS-02: 3초 이내 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimilarityResultDto {
    private String userId;
    private Double similarityScore; // 0.0 ~ 1.0
    private Boolean isMatch; // threshold > 0.85
    private Long processingTimeMs;
    private String modelVersion;
    
    public static SimilarityResultDto match(String userId, Double score, Long processingTime, String modelVersion) {
        return SimilarityResultDto.builder()
                .userId(userId)
                .similarityScore(score)
                .isMatch(true)
                .processingTimeMs(processingTime)
                .modelVersion(modelVersion)
                .build();
    }
    
    public static SimilarityResultDto noMatch(Double score, Long processingTime, String modelVersion) {
        return SimilarityResultDto.builder()
                .userId(null)
                .similarityScore(score)
                .isMatch(false)
                .processingTimeMs(processingTime)
                .modelVersion(modelVersion)
                .build();
    }
}

