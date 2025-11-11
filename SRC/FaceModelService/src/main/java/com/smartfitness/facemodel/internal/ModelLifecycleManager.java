package com.smartfitness.facemodel.internal;

import com.smartfitness.facemodel.model.ModelVersion;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 모델 라이프사이클 관리자 - 모델 버전 관리 및 배포
 */
public class ModelLifecycleManager {
    private Map<String, ModelVersion> modelVersions;
    private String activeModelVersion;

    public ModelLifecycleManager() {
        this.modelVersions = new LinkedHashMap<>();
    }

    /**
     * 새로운 모델 로드 및 활성화
     */
    public void loadNewModel(byte[] modelBinary, String version, String description) {
        ModelVersion newModel = new ModelVersion(version, modelBinary, "N/A");
        newModel.setDescription(description);
        newModel.setDeployedAt(LocalDateTime.now());

        // 기존 활성 모델 비활성화
        if (activeModelVersion != null && modelVersions.containsKey(activeModelVersion)) {
            modelVersions.get(activeModelVersion).setIsActive(false);
        }

        // 새 모델 활성화
        newModel.setIsActive(true);
        modelVersions.put(version, newModel);
        this.activeModelVersion = version;
    }

    /**
     * 이전 모델로 롤백
     */
    public Boolean rollbackToPreviousModel() {
        if (modelVersions.isEmpty()) {
            return false;
        }

        // 버전 순서로 마지막에서 두 번째 모델 찾기
        ModelVersion previousModel = null;
        for (ModelVersion model : modelVersions.values()) {
            if (!model.getIsActive()) {
                previousModel = model;
            }
        }

        if (previousModel == null) {
            return false;
        }

        // 현재 활성 모델 비활성화
        if (activeModelVersion != null && modelVersions.containsKey(activeModelVersion)) {
            modelVersions.get(activeModelVersion).setIsActive(false);
        }

        // 이전 모델 활성화
        previousModel.setIsActive(true);
        this.activeModelVersion = previousModel.getVersion();
        return true;
    }

    /**
     * 활성 모델 버전 조회
     */
    public String getActiveModelVersion() {
        return activeModelVersion;
    }

    /**
     * 특정 버전의 모델 조회
     */
    public ModelVersion getModelMetadata(String version) {
        return modelVersions.get(version);
    }

    /**
     * 활성 모델 객체 조회
     */
    public ModelVersion getActiveModel() {
        if (activeModelVersion == null) {
            return null;
        }
        return modelVersions.get(activeModelVersion);
    }

    /**
     * 모든 모델 버전 조회
     */
    public Map<String, ModelVersion> getAllModelVersions() {
        return new LinkedHashMap<>(modelVersions);
    }
}
