package com.smartfitness.helper.internal.logic;

import com.smartfitness.event.TaskSubmittedEvent;
import com.smartfitness.helper.model.TaskRegistrationResult;
import com.smartfitness.helper.model.TaskSubmission;
import com.smartfitness.helper.ports.IAIPanDokuService;
import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.messaging.IMessagePublisherService;

/**
 * TaskManagementManager: Handles task submission workflow and state changes.
 */
public class TaskManagementManager {
    private final IHelperRepository repository;
    private final IMessagePublisherService messagePublisher;
    private final IAIPanDokuService aiPanDokuClient;

    public TaskManagementManager(IHelperRepository repository,
                                 IMessagePublisherService messagePublisher,
                                 IAIPanDokuService aiPanDokuClient) {
        this.repository = repository;
        this.messagePublisher = messagePublisher;
        this.aiPanDokuClient = aiPanDokuClient;
    }

    /**
     * Process task submission and request asynchronous AI review (UC-12, UC-13).
     */
    public TaskRegistrationResult processTaskSubmission(TaskSubmission submission) {
        repository.save(submission);

        messagePublisher.publishEvent(
            "tasks.submitted",
            new TaskSubmittedEvent(submission.getId(), submission.getHelperId(), submission.getImageUrl())
        );

        aiPanDokuClient.requestInitialPanDoku(submission.getId(), submission.getImageUrl());

        return TaskRegistrationResult.SUCCESS;
    }
}
