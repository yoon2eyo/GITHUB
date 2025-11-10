package com.smartfitness.mlo.internal.service;

import com.smartfitness.mlo.internal.verification.AccuracyVerifier;
import com.smartfitness.mlo.internal.verification.PerformanceVerifier;

/**
 * ModelVerificationService: Composite service for all model verification tasks.
 * Facade pattern implementation for verification operations.
 */
public class ModelVerificationService implements IModelVerificationService {
    private final AccuracyVerifier accuracyVerifier;
    private final PerformanceVerifier performanceVerifier;

    public ModelVerificationService(AccuracyVerifier accuracyVerifier,
                                    PerformanceVerifier performanceVerifier) {
        this.accuracyVerifier = accuracyVerifier;
        this.performanceVerifier = performanceVerifier;
    }

    @Override
    public boolean verifyModelAccuracy(byte[] modelBinary, double threshold) {
        return accuracyVerifier.verifyAccuracy(modelBinary, threshold);
    }

    @Override
    public boolean verifyModelPerformance(byte[] modelBinary, int maxLatencyMs) {
        return performanceVerifier.verifyPerformance(modelBinary, maxLatencyMs);
    }

    @Override
    public String getVerificationReport(byte[] modelBinary) {
        StringBuilder report = new StringBuilder();
        report.append("Accuracy Report: ").append(accuracyVerifier.getDetailedReport(modelBinary)).append("\n");
        report.append("Performance Report: ").append(performanceVerifier.getDetailedReport(modelBinary));
        return report.toString();
    }
}