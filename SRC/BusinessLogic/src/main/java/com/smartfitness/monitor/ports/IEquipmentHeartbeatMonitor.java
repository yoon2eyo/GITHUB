package com.smartfitness.monitor.ports;

import java.time.LocalDateTime;

public interface IEquipmentHeartbeatMonitor {
    /**
     * 장비의 마지막 상태 체크 시간을 조회합니다.
     * @param equipmentId 장비 ID
     * @return 마지막 상태 체크 시간
     */
    LocalDateTime getLastStatusCheckTime(String equipmentId);

    /**
     * 장비가 현재 활성 상태인지 확인합니다.
     * @param equipmentId 장비 ID
     * @return 활성 상태 여부
     */
    boolean isEquipmentActive(String equipmentId);

    /**
     * 장비의 타임아웃 상태를 설정합니다.
     * @param equipmentId 장비 ID
     * @param timeout 타임아웃 발생 여부
     */
    void setTimeoutStatus(String equipmentId, boolean timeout);
}