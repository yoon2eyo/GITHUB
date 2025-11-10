package com.smartfitness.search.internal.scheduling;

import com.smartfitness.event.BranchPreferenceCreatedEvent;
import com.smartfitness.search.internal.consumer.PreferenceMatchConsumer;
import com.smartfitness.search.ports.ISearchRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Executes buffered preference-matching jobs during the configured scheduling window (DD-07).
 */
public class PreferenceMatchScheduler implements AutoCloseable {
    private final PreferenceMatchConsumer consumer;
    private final ISearchRepository repository;
    private final SchedulingPolicyWindow window;
    private final ScheduledExecutorService executor;
    private final Clock clock;

    public PreferenceMatchScheduler(PreferenceMatchConsumer consumer,
                                    ISearchRepository repository,
                                    SchedulingPolicyWindow window) {
        this(consumer, repository, window, Clock.systemDefaultZone());
    }

    public PreferenceMatchScheduler(PreferenceMatchConsumer consumer,
                                    ISearchRepository repository,
                                    SchedulingPolicyWindow window,
                                    Clock clock) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.window = Objects.requireNonNull(window, "window");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start(long initialDelaySeconds, long intervalSeconds) {
        executor.scheduleAtFixedRate(this::processBufferedEvents,
            initialDelaySeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    private void processBufferedEvents() {
        Instant now = clock.instant();
        if (!window.isWithinWindow(now)) {
            return;
        }

        List<BranchPreferenceCreatedEvent> events = consumer.drainBufferedEvents();
        if (events.isEmpty()) {
            return;
        }

        for (BranchPreferenceCreatedEvent event : events) {
            repository.executeMatchQuery(event.getKeywords());
        }
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
