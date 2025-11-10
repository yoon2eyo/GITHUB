package com.smartfitness.access.ports;

import com.smartfitness.access.model.AccessAttempt;

public interface IAccessEventHandler {
    /**
     * 출입 시도 이벤트를 처리합니다.
     * @param accessAttempt 출입 시도 정보
     */
    void handleAccessAttempt(AccessAttempt accessAttempt);

    /**
     * 출입 결과 이벤트를 처리합니다.
     * @param accessAttempt 출입 결과 정보
     */
    void handleAccessResult(AccessAttempt accessAttempt);
}