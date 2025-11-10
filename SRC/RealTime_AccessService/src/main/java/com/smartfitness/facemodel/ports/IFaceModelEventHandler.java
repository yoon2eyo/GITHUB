package com.smartfitness.facemodel.ports;

import com.smartfitness.facemodel.model.FaceModelEvent;

public interface IFaceModelEventHandler {
    /**
     * 얼굴 인식 모델 이벤트를 처리합니다.
     * @param event 모델 관련 이벤트 정보
     */
    void handleModelEvent(FaceModelEvent event);

    /**
     * 얼굴 인식 검증 결과 이벤트를 처리합니다.
     * @param event 검증 결과 이벤트 정보
     */
    void handleVerificationEvent(FaceModelEvent event);
}