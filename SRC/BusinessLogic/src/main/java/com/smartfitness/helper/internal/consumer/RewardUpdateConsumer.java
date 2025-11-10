package com.smartfitness.helper.internal.consumer;

import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.TaskConfirmedEvent;
import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.messaging.IMessageSubscriptionService;

import java.util.Objects;

/**
 * Consumes reward confirmation events and updates helper balances asynchronously.
 */
public class RewardUpdateConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private final IHelperRepository helperRepository;
    private final String topicName;

    public RewardUpdateConsumer(IMessageSubscriptionService subscriptionService,
                                IHelperRepository helperRepository) {
        this(subscriptionService, helperRepository, "tasks.confirmed");
    }

    public RewardUpdateConsumer(IMessageSubscriptionService subscriptionService,
                                IHelperRepository helperRepository,
                                String topicName) {
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
        this.helperRepository = Objects.requireNonNull(helperRepository, "helperRepository");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
    }

    public void register() {
        subscriptionService.subscribeToTopic(topicName, this::handleTaskConfirmation);
    }

    private void handleTaskConfirmation(DomainEvent event) {
        if (!(event instanceof TaskConfirmedEvent confirmedEvent)) {
            return;
        }

        try {
            if (confirmedEvent.isApproved()) {
                helperRepository.updateBalance(confirmedEvent.getHelperId(), confirmedEvent.getRewardAmount());
            }
            helperRepository.updateTaskStatus(
                confirmedEvent.getTaskId(),
                confirmedEvent.isApproved() ? "Approved" : "Denied",
                null
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to update reward for task " + confirmedEvent.getTaskId(), ex);
        }
    }
}
