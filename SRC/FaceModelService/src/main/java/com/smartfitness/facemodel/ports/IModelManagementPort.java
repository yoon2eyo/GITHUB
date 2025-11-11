package com.smartfitness.facemodel.ports;

/**
 * 모델 관리 인터페이스
 * 모델 로드, 배포, 롤백 기능
 */
public interface IModelManagementPort {
    /**
     * 새로운 모델 로드
     * @param modelBinary 모델 바이너리 데이터
     * @param version 모델 버전
     */
    void loadNewModel(byte[] modelBinary, String version);

    /**
     * 이전 모델로 롤백
     */
    void rollbackToPreviousModel();

    /**
     * 현재 활성 모델 버전 조회
     * @return 모델 버전
     */
    String getActiveModelVersion();

    /**
     * 모델 버전별 메타데이터 조회
     * @param version 모델 버전
     * @return 모델 메타데이터
     */
    ModelMetadata getModelMetadata(String version);
}

/**
 * 모델 메타데이터
 */
class ModelMetadata {
    private String version;
    private String createdAt;
    private String accuracy;
    private String description;

    // Getters and Setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getAccuracy() { return accuracy; }
    public void setAccuracy(String accuracy) { this.accuracy = accuracy; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
