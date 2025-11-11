package com.smartfitness.facemodel.ports;

import com.smartfitness.facemodel.model.FaceModelData;
import java.util.Optional;
import java.util.List;

/**
 * 얼굴 모델 데이터 저장소 인터페이스
 */
public interface IFaceModelRepository {
    /**
     * 사용자 얼굴 벡터 저장
     * @param faceModelData 얼굴 모델 데이터
     */
    void saveFaceVector(FaceModelData faceModelData);

    /**
     * 사용자 얼굴 벡터 조회
     * @param userId 사용자 ID
     * @return 얼굴 벡터 리스트
     */
    List<FaceModelData> findFaceVectorsByUserId(String userId);

    /**
     * 사용자의 모든 얼굴 벡터 삭제
     * @param userId 사용자 ID
     */
    void deleteFaceVectorsByUserId(String userId);

    /**
     * 얼굴 벡터 조회
     * @param vectorId 벡터 ID
     * @return 얼굴 벡터 (Optional)
     */
    Optional<FaceModelData> findById(String vectorId);
}
