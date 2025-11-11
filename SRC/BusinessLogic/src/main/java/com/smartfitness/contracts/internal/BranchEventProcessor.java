package com.smartfitness.contracts.internal;

import com.smartfitness.contracts.ports.IBranchEventConsumer;
import com.smartfitness.event.IDomainEvent;
import com.smartfitness.messaging.ports.IMessageSubscriptionService;

/**
 * 지점 관련 이벤트 처리
 * 메시지 브로커로부터 이벤트를 수신하고 처리
 */
public class BranchEventProcessor implements IBranchEventConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private boolean registered = false;

    public BranchEventProcessor(IMessageSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void handleEvent(IDomainEvent event) {
        if (event == null) {
            return;
        }

        // 이벤트 타입에 따라 처리
        String eventType = event.getClass().getSimpleName();

        try {
            switch (eventType) {
                case "BranchOwnerCreatedEvent":
                    handleBranchOwnerCreatedEvent(event);
                    break;
                case "BranchInfoUpdatedEvent":
                    handleBranchInfoUpdatedEvent(event);
                    break;
                case "BranchPreferenceCreatedEvent":
                    handleBranchPreferenceCreatedEvent(event);
                    break;
                default:
                    // 알려지지 않은 이벤트는 무시
                    System.out.println("Unknown branch event: " + eventType);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error handling event " + eventType + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void register() {
        if (!registered) {
            subscriptionService.subscribe("branch.owner.created", this);
            subscriptionService.subscribe("branch.info.updated", this);
            subscriptionService.subscribe("preferences.created", this);
            registered = true;
            System.out.println("BranchEventProcessor registered for events");
        }
    }

    @Override
    public void unregister() {
        if (registered) {
            subscriptionService.unsubscribe("branch.owner.created", this);
            subscriptionService.unsubscribe("branch.info.updated", this);
            subscriptionService.unsubscribe("preferences.created", this);
            registered = false;
            System.out.println("BranchEventProcessor unregistered from events");
        }
    }

    /**
     * 지점주 생성 이벤트 처리
     */
    private void handleBranchOwnerCreatedEvent(IDomainEvent event) {
        System.out.println("Processing BranchOwnerCreatedEvent");
        // 실제 구현에서는 관련 처리 로직 추가
        // 예: 통계 업데이트, 알림 전송 등
    }

    /**
     * 지점 정보 업데이트 이벤트 처리
     */
    private void handleBranchInfoUpdatedEvent(IDomainEvent event) {
        System.out.println("Processing BranchInfoUpdatedEvent");
        // 실제 구현에서는 관련 처리 로직 추가
        // 예: 캐시 무효화, 인덱싱 업데이트 등
    }

    /**
     * 지점 선호도 생성 이벤트 처리
     */
    private void handleBranchPreferenceCreatedEvent(IDomainEvent event) {
        System.out.println("Processing BranchPreferenceCreatedEvent");
        // 실제 구현에서는 관련 처리 로직 추가
        // 예: 지점별 고객 선호도 집계 등
    }
}
