package com.smartfitness.facemodel.controller;

import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;

/**
 * Interface Layer: FaceModel Service API Interface
 * Reference: 12_FaceModelServiceComponent.puml (IFaceModelServiceApi)
 */
public interface IFaceModelServiceApi {
    SimilarityResultDto calculateSimilarity(FaceModelIPCHandler.SimilarityRequest request);
    FaceVectorDto extractFeatures(byte[] photoBytes);
}

