package com.smartfitness.search;

import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.messaging.IMessageSubscriptionService;
import com.smartfitness.search.internal.consumer.PreferenceMatchConsumer;
import com.smartfitness.search.internal.logic.BranchContentService;
import com.smartfitness.search.internal.scheduling.PreferenceMatchScheduler;
import com.smartfitness.search.internal.scheduling.SchedulingPolicyWindow;
import com.smartfitness.search.ports.ILLMAnalysisService;
import com.smartfitness.search.ports.ISearchRepository;
import com.smartfitness.search.ports.ISearchServiceApi;
import java.util.Objects;

/**
 * Composition root that wires the Branch Content Service components together.
 */
public final class BranchContentServiceComponent implements AutoCloseable {
    private final BranchContentService branchContentService;
    private final PreferenceMatchScheduler scheduler;

    private BranchContentServiceComponent(BranchContentService branchContentService,
                                         PreferenceMatchScheduler scheduler) {
        this.branchContentService = branchContentService;
        this.scheduler = scheduler;
    }

    public static BranchContentServiceComponent bootstrap(ISearchRepository repository,
                                                          ILLMAnalysisService llmClient,
                                                          IMessagePublisherService messagePublisher,
                                                          IMessageSubscriptionService subscriptionService,
                                                          SchedulingPolicyWindow schedulingWindow,
                                                          long schedulerInitialDelaySeconds,
                                                          long schedulerIntervalSeconds) {
        Objects.requireNonNull(repository, "repository");
        Objects.requireNonNull(llmClient, "llmClient");
        Objects.requireNonNull(messagePublisher, "messagePublisher");
        Objects.requireNonNull(subscriptionService, "subscriptionService");
        Objects.requireNonNull(schedulingWindow, "schedulingWindow");

        PreferenceMatchConsumer consumer =
            new PreferenceMatchConsumer(subscriptionService, repository);
        consumer.register();

        PreferenceMatchScheduler scheduler =
            new PreferenceMatchScheduler(consumer, repository, schedulingWindow);
        scheduler.start(schedulerInitialDelaySeconds, schedulerIntervalSeconds);

        BranchContentService service = new BranchContentService(repository, llmClient, messagePublisher);
        return new BranchContentServiceComponent(service, scheduler);
    }

    public ISearchServiceApi api() {
        return branchContentService;
    }

    @Override
    public void close() {
        scheduler.close();
    }
}
