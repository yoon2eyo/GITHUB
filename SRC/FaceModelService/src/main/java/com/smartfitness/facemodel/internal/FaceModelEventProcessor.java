package com.smartfitness.facemodel.internal;

import com.smartfitness.event.DomainEvent;
import com.smartfitness.facemodel.ports.IFaceModelEventHandler;

/**
 * 얼굴 모델 서비스 이벤트 프로세서
 */
public class FaceModelEventProcessor implements IFaceModelEventHandler {
    private Boolean isRegistered = false;

    @Override
    public void handleEvent(DomainEvent event) {
        if (event == null) {
            return;
        }

        String eventType = event.getClass().getSimpleName();

        switch (eventType) {
            case "AccessAttemptEvent":
                handleAccessAttempt(event);
                break;
            case "EquipmentFaultDetectedEvent":
                handleEquipmentFault(event);
                break;
            default:
                // Other events are ignored
                break;
        }
    }

    @Override
    public void register() {
        this.isRegistered = true;
    }

    @Override
    public void unregister() {
        this.isRegistered = false;
    }

    /**
     * 접근 시도 이벤트 처리
     */
    private void handleAccessAttempt(DomainEvent event) {
        // 추후 구현: AccessAttemptEvent 처리
        // 얼굴 인식 결과 기록, 통계 수집 등
    }

    /**
     * 장비 오류 이벤트 처리
     */
    private void handleEquipmentFault(DomainEvent event) {
        // 추후 구현: EquipmentFaultDetectedEvent 처리
        // 카메라 오류 발생 시 모델 상태 체크
    }

    public Boolean isRegistered() {
        return isRegistered;
    }
}
