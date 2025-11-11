package com.smartfitness.facemodel.ports;

import com.smartfitness.facemodel.model.ModelVersion;
import java.util.Optional;
import java.util.List;

/**
 * 모델 버전 저장소 인터페이스
 */
public interface IModelVersionRepository {
    /**
     * 모델 버전 저장
     * @param modelVersion 모델 버전 정보
     */
    void save(ModelVersion modelVersion);

    /**
     * 모델 버전 조회
     * @param version 버전명
     * @return 모델 버전 (Optional)
     */
    Optional<ModelVersion> findByVersion(String version);

    /**
     * 모든 모델 버전 조회 (최신순)
     * @return 모델 버전 리스트
     */
    List<ModelVersion> findAllOrderByCreatedDesc();

    /**
     * 현재 활성 모델 조회
     * @return 활성 모델 버전
     */
    Optional<ModelVersion> findActiveModel();

    /**
     * 모델 활성화
     * @param version 버전명
     */
    void activateModel(String version);

    /**
     * 모델 버전 삭제
     * @param version 버전명
     */
    void deleteByVersion(String version);
}
