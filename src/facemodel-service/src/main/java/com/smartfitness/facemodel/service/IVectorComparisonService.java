package com.smartfitness.facemodel.service;

import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;

/**
 * Business Layer Interface: Vector Comparison Service
 * Reference: 12_FaceModelServiceComponent.puml
 */
public interface IVectorComparisonService {
    SimilarityResultDto calculateSimilarity(byte[] requestedPhoto, FaceVectorDto storedVector);
    FaceVectorDto extractFeatures(byte[] photoBytes);
}

