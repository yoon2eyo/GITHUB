package com.smartfitness.monitor.internal.logic;

import com.smartfitness.event.EquipmentFaultDetectedEvent;
import com.smartfitness.messaging.IMessagePublisherService;
import com.smartfitness.monitor.ports.IMonitorRepository;
import com.smartfitness.monitor.ports.IMonitoringTriggerService;
import java.util.Date;
import java.util.List;

/**
 * HeartbeatChecker: Periodically checks last report times and emits alerts on timeout.
 */
public class HeartbeatChecker implements IMonitoringTriggerService {
    private final IMonitorRepository repository;
    private final IMessagePublisherService publisher;
    private static final long TIMEOUT_THRESHOLD_MS = 30_000L; // 30s

    private final List<String> allEquipmentIds = List.of("GATE-01", "CAM-01", "GATE-02");

    public HeartbeatChecker(IMonitorRepository repository, IMessagePublisherService publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Override
    public void triggerMonitorCheck() {
        long now = System.currentTimeMillis();
        for (String equipmentId : allEquipmentIds) {
            Date last = repository.findLastReportTime(equipmentId);
            if (last == null || (now - last.getTime() > TIMEOUT_THRESHOLD_MS)) {
                publisher.publishEvent("faults", new EquipmentFaultDetectedEvent(equipmentId, "Heartbeat Timeout"));
                repository.saveNotificationLog(equipmentId, "Heartbeat Timeout");
            }
        }
    }
}
