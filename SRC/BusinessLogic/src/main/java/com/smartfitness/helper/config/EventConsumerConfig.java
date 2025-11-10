package com.smartfitness.helper.config;

import com.smartfitness.helper.ports.IHelperEventConsumer;
import com.smartfitness.messaging.IMessageSubscriptionService;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * Helper 서비스의 이벤트 소비자들을 메시지 브로커에 등록하는 설정 클래스입니다.
 */
@Configuration
public class EventConsumerConfig {
    
    private final List<IHelperEventConsumer> eventConsumers;
    private final IMessageSubscriptionService subscriptionService;
    
    public EventConsumerConfig(
            List<IHelperEventConsumer> eventConsumers,
            IMessageSubscriptionService subscriptionService) {
        this.eventConsumers = eventConsumers;
        this.subscriptionService = subscriptionService;
    }
    
    /**
     * 모든 이벤트 소비자들을 메시지 브로커에 등록합니다.
     */
    @PostConstruct
    public void registerEventConsumers() {
        for (IHelperEventConsumer consumer : eventConsumers) {
            // 각 소비자를 해당 토픽에 등록
            subscriptionService.subscribeToTopic(
                consumer.getSubscriptionTopic(),
                event -> consumer.handleEvent(event)
            );
        }
    }
}