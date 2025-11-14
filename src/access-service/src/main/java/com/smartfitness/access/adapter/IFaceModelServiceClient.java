package com.smartfitness.access.adapter;

import com.smartfitness.common.dto.FaceVectorDto;
import com.smartfitness.common.dto.SimilarityResultDto;

/**
 * System Interface Layer: FaceModel Service Client Interface
 * DD-05: IPC/gRPC communication (Same Physical Node)
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
public interface IFaceModelServiceClient {
    SimilarityResultDto calculateSimilarity(byte[] requestedPhoto, FaceVectorDto storedVector);
}

