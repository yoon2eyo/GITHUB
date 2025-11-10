package com.smartfitness.monitor.ports;

import com.smartfitness.event.DomainEvent;
import java.util.List;

public interface IEquipmentEventHandler {
    /**
     * 장비 상태 관련 이벤트를 처리합니다.
     * @param event 처리할 이벤트
     */
    void handleStatusEvent(DomainEvent event);

    /**
     * 특정 장비의 이벤트 히스토리를 조회합니다.
     * @param equipmentId 장비 ID
     * @return 이벤트 목록
     */
    List<DomainEvent> getStatusEventHistory(String equipmentId);
}