package com.smartfitness.helper.internal.consumer;

import com.smartfitness.ai.ports.IPanDokuModelService;
import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.TaskSubmittedEvent;
import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.messaging.IMessageSubscriptionService;

import java.util.Objects;

/**
 * Consumes task submission events and triggers the first AI review pipeline through
 * the PanDoku model service. Results are persisted back to the helper repository so
 * that human reviewers can continue the workflow.
 */
public class AIPanDokuConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private final IPanDokuModelService panDokuModelService;
    private final IHelperRepository helperRepository;
    private final String topicName;

    public AIPanDokuConsumer(IMessageSubscriptionService subscriptionService,
                             IPanDokuModelService panDokuModelService,
                             IHelperRepository helperRepository) {
        this(subscriptionService, panDokuModelService, helperRepository, "tasks.submitted");
    }

    public AIPanDokuConsumer(IMessageSubscriptionService subscriptionService,
                             IPanDokuModelService panDokuModelService,
                             IHelperRepository helperRepository,
                             String topicName) {
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
        this.panDokuModelService = Objects.requireNonNull(panDokuModelService, "panDokuModelService");
        this.helperRepository = Objects.requireNonNull(helperRepository, "helperRepository");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
    }

    /**
     * Register the consumer with the broker. The caller may invoke
     * {@link IMessageSubscriptionService#startListening()} once all consumers are registered.
     */
    public void register() {
        subscriptionService.subscribeToTopic(topicName, this::handleTaskSubmission);
    }

    private void handleTaskSubmission(DomainEvent event) {
        if (!(event instanceof TaskSubmittedEvent submittedEvent)) {
            return;
        }

        try {
            // Use generateVector to process face image and extract features
            // (Note: In production, imageUrl would be downloaded to get byte array)
            byte[] imageData = downloadImage(submittedEvent.getImageUrl());
            double[] vectorResult = panDokuModelService.generateVector(imageData);
            String aiResult = formatVector(vectorResult);
            helperRepository.updateTaskStatus(submittedEvent.getTaskId(), aiResult, null);
        } catch (Exception ex) {
            // The message broker can retry delivery based on the thrown exception.
            throw new IllegalStateException("Failed to process task " + submittedEvent.getTaskId(), ex);
        }
    }
    
    private byte[] downloadImage(String imageUrl) {
        // TODO: Implement image download logic
        return new byte[0];
    }
    
    private String formatVector(double[] vector) {
        // TODO: Format vector for storage
        return "";
    }
}
