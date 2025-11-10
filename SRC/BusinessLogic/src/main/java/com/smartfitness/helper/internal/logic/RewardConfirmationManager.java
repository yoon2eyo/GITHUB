package com.smartfitness.helper.internal.logic;

import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.messaging.IMessagePublisherService;

import com.smartfitness.event.TaskConfirmedEvent;

/**
 * RewardConfirmationManager: Executes reward logic upon admin confirmation.
 */
public class RewardConfirmationManager {
    private final IHelperRepository repository;
    private final IMessagePublisherService messagePublisher;

    public RewardConfirmationManager(IHelperRepository repository,
                                     IMessagePublisherService messagePublisher) {
        this.repository = repository;
        this.messagePublisher = messagePublisher;
    }

    /**
     * Handle final approval and update balances/status (UC-14, UC-16).
     */
    public void handleTaskConfirmation(Long taskId, Long helperId, boolean isApproved, double rewardAmount) {
        if (isApproved) {
            repository.updateBalance(helperId, rewardAmount);
        }
        repository.updateTaskStatus(taskId, isApproved ? "Approved" : "Denied", null);

        messagePublisher.publishEvent(
            "tasks.confirmed",
            new TaskConfirmedEvent(taskId, helperId, rewardAmount, isApproved)
        );
    }
}
