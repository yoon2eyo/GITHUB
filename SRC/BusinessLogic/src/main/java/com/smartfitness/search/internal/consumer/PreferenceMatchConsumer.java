package com.smartfitness.search.internal.consumer;

import com.smartfitness.event.BranchPreferenceCreatedEvent;
import com.smartfitness.event.DomainEvent;
import com.smartfitness.messaging.IMessageSubscriptionService;
import com.smartfitness.search.ports.ISearchRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Consumes branch preference creation events and proactively executes matching
 * queries so that recommended content stays fresh without blocking the original
 * request thread.
 */
public class PreferenceMatchConsumer {
    private final IMessageSubscriptionService subscriptionService;
    private final ISearchRepository searchRepository;
    private final String topicName;
    private final boolean immediateProcessing;
    private final Queue<BranchPreferenceCreatedEvent> bufferedEvents = new ConcurrentLinkedQueue<>();

    public PreferenceMatchConsumer(IMessageSubscriptionService subscriptionService,
                                   ISearchRepository searchRepository) {
        this(subscriptionService, searchRepository, "preferences", false);
    }

    public PreferenceMatchConsumer(IMessageSubscriptionService subscriptionService,
                                   ISearchRepository searchRepository,
                                   String topicName) {
        this(subscriptionService, searchRepository, topicName, false);
    }

    public PreferenceMatchConsumer(IMessageSubscriptionService subscriptionService,
                                   ISearchRepository searchRepository,
                                   String topicName,
                                   boolean immediateProcessing) {
        this.subscriptionService = Objects.requireNonNull(subscriptionService, "subscriptionService");
        this.searchRepository = Objects.requireNonNull(searchRepository, "searchRepository");
        this.topicName = Objects.requireNonNull(topicName, "topicName");
        this.immediateProcessing = immediateProcessing;
    }

    public void register() {
        subscriptionService.subscribeToTopic(topicName, this::handlePreferenceCreated);
    }

    /**
     * Drain buffered events so that the scheduling policy executor can process them in bulk.
     */
    public List<BranchPreferenceCreatedEvent> drainBufferedEvents() {
        List<BranchPreferenceCreatedEvent> drained = new ArrayList<>();
        BranchPreferenceCreatedEvent event;
        while ((event = bufferedEvents.poll()) != null) {
            drained.add(event);
        }
        return drained;
    }

    private void handlePreferenceCreated(DomainEvent event) {
        if (!(event instanceof BranchPreferenceCreatedEvent preferenceCreatedEvent)) {
            return;
        }

        bufferedEvents.add(preferenceCreatedEvent);

        if (immediateProcessing) {
            searchRepository.executeMatchQuery(preferenceCreatedEvent.getKeywords());
        }
    }
}
