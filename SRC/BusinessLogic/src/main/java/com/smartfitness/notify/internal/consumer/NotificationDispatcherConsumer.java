package com.smartfitness.notify.internal.consumer;

import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.EquipmentFaultDetectedEvent;
import com.smartfitness.messaging.IMessageSubscriptionService;
import com.smartfitness.notify.ports.IPushNotificationGateway;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NotificationDispatcherConsumer: Subscribes to "faults" topic and invokes the push gateway.
 * Tactic: Message Based, Use an Intermediary (DD-04/DD-08).
 */
public class NotificationDispatcherConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private final IPushNotificationGateway pushGateway;
    private final String topicName;
    private final AtomicBoolean registered = new AtomicBoolean(false);

    public NotificationDispatcherConsumer(IMessageSubscriptionService subscriptionService,
                                          IPushNotificationGateway pushGateway) {
        this(subscriptionService, pushGateway, "faults");
    }

    public NotificationDispatcherConsumer(IMessageSubscriptionService subscriptionService,
                                          IPushNotificationGateway pushGateway,
                                          String topicName) {
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
        this.pushGateway = Objects.requireNonNull(pushGateway, "pushGateway");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
    }

    /**
     * Register the consumer with the Message Broker. Idempotent.
     */
    public void register() {
        if (registered.compareAndSet(false, true)) {
            subscriptionService.subscribeToTopic(topicName, this::handleFaultEvent);
        }
    }

    private void handleFaultEvent(DomainEvent event) {
        if (!(event instanceof EquipmentFaultDetectedEvent faultEvent)) {
            return;
        }

        // In production the recipient list would be looked up from configuration.
        String title = "[FAULT] " + faultEvent.getEquipmentId();
        String body = "Reason: " + faultEvent.getReason();
        pushGateway.sendPushNotification("ADMIN", title, body);
    }
}
