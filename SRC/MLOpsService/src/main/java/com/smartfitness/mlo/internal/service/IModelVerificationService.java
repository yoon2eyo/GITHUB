package com.smartfitness.mlo.internal.service;

/**
 * IModelVerificationService: Validates model quality and performance.
 */
public interface IModelVerificationService {
    /**
     * Verify model accuracy meets requirements.
     */
    boolean verifyModelAccuracy(byte[] modelBinary, double threshold);
    
    /**
     * Verify model performance meets requirements.
     */
    boolean verifyModelPerformance(byte[] modelBinary, int maxLatencyMs);
    
    /**
     * Get detailed verification report.
     */
    String getVerificationReport(byte[] modelBinary);
}