package com.smartfitness.monitor.ports;

import com.smartfitness.monitor.model.EquipmentStatus;

public interface IEquipmentStatusUpdater {
    /**
     * 장비의 현재 상태를 업데이트합니다.
     * @param equipmentId 장비 ID
     * @param status 새로운 상태 정보
     */
    void updateEquipmentStatus(String equipmentId, EquipmentStatus status);

    /**
     * 장비의 장애 상태를 기록합니다.
     * @param equipmentId 장비 ID
     * @param faultReason 장애 발생 사유
     */
    void recordFault(String equipmentId, String faultReason);
}