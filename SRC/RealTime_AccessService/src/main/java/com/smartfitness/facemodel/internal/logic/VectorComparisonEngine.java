package com.smartfitness.facemodel.internal.logic;

import com.smartfitness.common.model.FaceVector;
import com.smartfitness.facemodel.ports.IFaceModelService;
import com.smartfitness.mlo.model.LoadedModel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * VectorComparisonEngine: Implements face vector comparison algorithms.
 * Tactic: Pipeline Optimization (DD-05), Introduce Concurrency.
 */
public class VectorComparisonEngine implements IFaceModelService {
    // Active model reference for hot-swap capability.
    public static final AtomicReference<LoadedModel> activeModel = new AtomicReference<>();

    // History of loaded models to support rollback by version.
    public static final ConcurrentHashMap<String, LoadedModel> modelHistory = new ConcurrentHashMap<>();

    private static final ExecutorService PIPELINE_POOL =
        Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));

    @Override
    public double calculateSimilarityScore(FaceVector requestedVector, FaceVector storedVector) {
        LoadedModel model = activeModel.get();
        if (model == null) {
            throw new IllegalStateException("Face Model is not loaded.");
        }

        CompletableFuture<float[]> requestedStage =
            CompletableFuture.supplyAsync(() -> extractFeatures(requestedVector), PIPELINE_POOL);
        CompletableFuture<float[]> storedStage =
            CompletableFuture.supplyAsync(() -> extractFeatures(storedVector), PIPELINE_POOL);

        return requestedStage
            .thenCombine(storedStage, this::cosineSimilarity)
            .thenApply(score -> applyThresholds(score, model))
            .join();
    }

    private float[] extractFeatures(FaceVector vector) {
        byte[] raw = vector.getData();
        int length = Math.min(raw.length, 512);
        float[] features = new float[length];
        for (int i = 0; i < length; i++) {
            features[i] = (raw[i] & 0xFF) / 255f;
        }
        return features;
    }

    private double cosineSimilarity(float[] requested, float[] stored) {
        int length = Math.min(requested.length, stored.length);
        double dot = 0;
        double reqMag = 0;
        double storedMag = 0;
        for (int i = 0; i < length; i++) {
            dot += requested[i] * stored[i];
            reqMag += requested[i] * requested[i];
            storedMag += stored[i] * stored[i];
        }
        double denominator = Math.sqrt(reqMag) * Math.sqrt(storedMag);
        return denominator == 0 ? 0 : dot / denominator;
    }

    private double applyThresholds(double score, LoadedModel model) {
        // Example post-processing: apply a margin based on model version length to keep the example deterministic.
        double margin = Math.min(0.05, model.getVersion().length() * 0.001);
        double adjusted = Math.max(0, Math.min(1, score - margin));
        return adjusted;
    }
}
