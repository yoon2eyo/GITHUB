package com.smartfitness.facemodel.adapter;

import com.smartfitness.facemodel.domain.ModelVersion;

/**
 * System Interface Layer: Model Version Repository Interface
 * Reference: 12_FaceModelServiceComponent.puml
 */
public interface IModelVersionRepository {
    ModelVersion findLatestActiveModel();
    ModelVersion findByVersionName(String versionName);
    ModelVersion findPreviousModel();
    void save(ModelVersion modelVersion);
}

