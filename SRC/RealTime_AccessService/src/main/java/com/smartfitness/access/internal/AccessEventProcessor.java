package com.smartfitness.access.internal;

import com.smartfitness.access.model.AccessAttempt;
import com.smartfitness.access.ports.IAccessEventHandler;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import com.smartfitness.access.ports.IAccessRepository;

public class AccessEventProcessor implements IAccessEventHandler {
    private final IMessagePublisherService messagePublisher;
    private final IAccessRepository accessRepository;

    public AccessEventProcessor(IMessagePublisherService messagePublisher, 
                              IAccessRepository accessRepository) {
        this.messagePublisher = messagePublisher;
        this.accessRepository = accessRepository;
    }

    @Override
    public void handleAccessAttempt(AccessAttempt accessAttempt) {
        // 출입 시도 이벤트 처리
        accessRepository.saveAccessAttempt(accessAttempt);
        
        // 관련 시스템에 이벤트 발행
        messagePublisher.publish("access.attempt", accessAttempt);
    }

    @Override
    public void handleAccessResult(AccessAttempt accessAttempt) {
        // 출입 결과 이벤트 처리
        accessRepository.updateAccessAttempt(accessAttempt);
        
        // 결과를 관련 시스템에 통지
        messagePublisher.publish("access.result", accessAttempt);
    }
}