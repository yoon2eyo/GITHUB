package com.smartfitness.facemodel.model;

/**
 * 얼굴 인증 결과 모델
 */
public class FaceVerificationResult {
    private Boolean verified;
    private Double similarityScore;
    private String reason;
    private Long processingTimeMs;

    public FaceVerificationResult() {
    }

    public FaceVerificationResult(Boolean verified, Double similarityScore, String reason, Long processingTimeMs) {
        this.verified = verified;
        this.similarityScore = similarityScore;
        this.reason = reason;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters and Setters
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public Double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(Double similarityScore) { this.similarityScore = similarityScore; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}
