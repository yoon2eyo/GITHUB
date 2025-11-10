package com.smartfitness.facemodel.internal;

import com.smartfitness.facemodel.model.FaceModelEvent;
import com.smartfitness.facemodel.ports.IFaceModelEventHandler;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import com.smartfitness.facemodel.ports.IFaceModelRepository;

public class FaceModelProcessor implements IFaceModelEventHandler {
    private final IMessagePublisherService messagePublisher;
    private final IFaceModelRepository modelRepository;

    public FaceModelProcessor(IMessagePublisherService messagePublisher,
                            IFaceModelRepository modelRepository) {
        this.messagePublisher = messagePublisher;
        this.modelRepository = modelRepository;
    }

    @Override
    public void handleModelEvent(FaceModelEvent event) {
        // 모델 관련 이벤트 처리
        modelRepository.saveModelEvent(event);
        
        // 관련 시스템에 이벤트 발행
        messagePublisher.publish("facemodel.event", event);
        
        // 모델 업데이트 이벤트인 경우 특별 처리
        if (event.getEventType() == FaceModelEvent.EventType.MODEL_UPDATED) {
            notifyModelUpdate(event);
        }
    }

    @Override
    public void handleVerificationEvent(FaceModelEvent event) {
        // 검증 결과 이벤트 처리
        modelRepository.saveVerificationEvent(event);
        
        // 검증 결과를 관련 시스템에 통지
        messagePublisher.publish("facemodel.verification", event);
    }

    private void notifyModelUpdate(FaceModelEvent event) {
        // MLOps 서비스에 모델 업데이트 통지
        messagePublisher.publish("mlops.model.updated", event);
    }
}