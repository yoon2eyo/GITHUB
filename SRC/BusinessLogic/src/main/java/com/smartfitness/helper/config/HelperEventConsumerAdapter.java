package com.smartfitness.helper.config;

import com.smartfitness.helper.ports.IHelperEventConsumer;
import com.smartfitness.messaging.IMessageSubscriptionService;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Helper 이벤트 소비자들을 메시지 브로커에 등록하는 컴포넌트입니다.
 * IHelperEventConsumer와 IMessageSubscriptionService 간의 어댑터 역할을 합니다.
 */
@Component
public class HelperEventConsumerAdapter {
    
    private final List<IHelperEventConsumer> eventConsumers;
    private final IMessageSubscriptionService subscriptionService;
    
    public HelperEventConsumerAdapter(
            List<IHelperEventConsumer> eventConsumers,
            IMessageSubscriptionService subscriptionService) {
        this.eventConsumers = eventConsumers;
        this.subscriptionService = subscriptionService;
    }
    
    /**
     * 각 Consumer를 해당하는 토픽에 구독 등록합니다.
     */
    @PostConstruct
    public void initializeSubscriptions() {
        eventConsumers.forEach(this::registerEventConsumer);
    }

    /**
     * 단일 Consumer를 메시지 브로커에 등록합니다.
     */
    private void registerEventConsumer(IHelperEventConsumer consumer) {
        String topic = consumer.getSubscriptionTopic();
        subscriptionService.subscribeToTopic(topic, consumer::handleEvent);
    }

    /**
     * 종료 시 모든 구독을 해제합니다.
     */
    public void shutdown() {
        eventConsumers.forEach(consumer -> 
            subscriptionService.unsubscribeFromTopic(
                consumer.getSubscriptionTopic(), 
                consumer::handleEvent
            )
        );
    }
}