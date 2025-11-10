package com.smartfitness.monitor.internal.logic;

import com.smartfitness.event.EquipmentFaultDetectedEvent;
import com.smartfitness.monitor.model.EquipmentStatusReport;
import com.smartfitness.monitor.ports.IEquipmentStatusService;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.monitor.ports.IMonitorRepository;

/**
 * StatusReceiverManager: Persists incoming reports and emits immediate fault events.
 */
public class StatusReceiverManager implements IEquipmentStatusService {
    private final IMonitorRepository repository;
    private final IMessagePublisherService publisher;

    public StatusReceiverManager(IMonitorRepository repository, IMessagePublisherService publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public void receiveStatusReport(EquipmentStatusReport report) {
        repository.saveStatus(report);

        if (report.isFault()) {
            publisher.publishEvent("faults", new EquipmentFaultDetectedEvent(report.getEquipmentId(), "Direct Fault Report"));
        }
    }
}
