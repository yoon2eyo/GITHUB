package com.smartfitness.helper;

import com.smartfitness.helper.model.HelperBalance;
import java.util.Optional;

/**
 * Helper의 보상 관리를 담당하는 서비스 인터페이스입니다.
 */
public interface IRewardConfirmationService {
    /**
     * Helper의 현재 보상 잔액을 조회합니다.
     */
    Optional<HelperBalance> getHelperBalance(Long helperId);
    
    /**
     * 작업 완료에 대한 보상을 처리합니다.
     */
    void processTaskReward(Long taskId, Long helperId, double amount);
}