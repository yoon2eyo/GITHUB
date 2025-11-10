package com.smartfitness.monitor.internal;

import com.smartfitness.monitor.model.EquipmentStatus;
import com.smartfitness.monitor.ports.IEquipmentStatusUpdater;
import com.smartfitness.monitor.ports.IEquipmentHeartbeatMonitor;
import com.smartfitness.monitor.ports.IEquipmentEventHandler;
import com.smartfitness.event.DomainEvent;
import com.smartfitness.event.EquipmentFaultDetectedEvent;
import com.smartfitness.monitor.model.EquipmentStatus.EquipmentStatusType;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentStatusTracker implements IEquipmentStatusUpdater, IEquipmentHeartbeatMonitor, IEquipmentEventHandler {
    private final Map<String, EquipmentStatus> equipmentStatusMap;
    private final Map<String, List<DomainEvent>> eventHistoryMap;

    public EquipmentStatusTracker() {
        this.equipmentStatusMap = new ConcurrentHashMap<>();
        this.eventHistoryMap = new ConcurrentHashMap<>();
    }

    @Override
    public void updateEquipmentStatus(String equipmentId, EquipmentStatus status) {
        status.setLastUpdateTime(LocalDateTime.now());
        equipmentStatusMap.put(equipmentId, status);
    }

    @Override
    public void recordFault(String equipmentId, String faultReason) {
        EquipmentStatus status = equipmentStatusMap.getOrDefault(equipmentId, new EquipmentStatus());
        status.setEquipmentId(equipmentId);
        status.setStatus(EquipmentStatusType.FAULT);
        status.setLastFaultReason(faultReason);
        status.setLastUpdateTime(LocalDateTime.now());
        equipmentStatusMap.put(equipmentId, status);

        // 장애 이벤트 생성 및 기록
        DomainEvent event = new EquipmentFaultDetectedEvent(equipmentId, faultReason);
        handleStatusEvent(event);
    }

    @Override
    public LocalDateTime getLastStatusCheckTime(String equipmentId) {
        return Optional.ofNullable(equipmentStatusMap.get(equipmentId))
                      .map(EquipmentStatus::getLastUpdateTime)
                      .orElse(null);
    }

    @Override
    public boolean isEquipmentActive(String equipmentId) {
        return Optional.ofNullable(equipmentStatusMap.get(equipmentId))
                      .map(status -> status.getStatus() == EquipmentStatusType.ACTIVE)
                      .orElse(false);
    }

    @Override
    public void setTimeoutStatus(String equipmentId, boolean timeout) {
        EquipmentStatus status = equipmentStatusMap.getOrDefault(equipmentId, new EquipmentStatus());
        status.setEquipmentId(equipmentId);
        status.setStatus(timeout ? EquipmentStatusType.TIMEOUT : EquipmentStatusType.ACTIVE);
        status.setLastUpdateTime(LocalDateTime.now());
        equipmentStatusMap.put(equipmentId, status);

        if (timeout) {
            recordFault(equipmentId, "Equipment timeout detected");
        }
    }

    @Override
    public void handleStatusEvent(DomainEvent event) {
        String equipmentId = null;
        if (event instanceof EquipmentFaultDetectedEvent) {
            equipmentId = ((EquipmentFaultDetectedEvent) event).getEquipmentId();
        }

        if (equipmentId != null) {
            eventHistoryMap.computeIfAbsent(equipmentId, k -> new ArrayList<>()).add(event);
        }
    }

    @Override
    public List<DomainEvent> getStatusEventHistory(String equipmentId) {
        return eventHistoryMap.getOrDefault(equipmentId, Collections.emptyList());
    }
}