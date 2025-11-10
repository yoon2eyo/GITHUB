package com.smartfitness.helper;

import com.smartfitness.helper.model.TaskSubmission;
import com.smartfitness.helper.model.TaskRegistrationResult;
import com.smartfitness.helper.model.HelperBalance;
import java.util.List;
import java.util.Optional;

/**
 * Helper 서비스의 공개 API를 정의하는 인터페이스입니다.
 */
public interface IHelperServiceApi {
    /**
     * Helper가 수행한 작업을 등록합니다.
     *
     * @param submission 제출할 작업 정보
     * @return 작업 등록 결과
     */
    TaskRegistrationResult registerTaskSubmission(TaskSubmission submission);

    /**
     * Helper의 현재 보상 잔액을 조회합니다.
     *
     * @param helperId Helper의 고유 ID
     * @return Helper의 보상 잔액 정보
     */
    Optional<HelperBalance> getHelperBalance(Long helperId);

    /**
     * 특정 지점의 검토가 필요한 작업 목록을 조회합니다.
     *
     * @param branchId 지점의 고유 ID
     * @return 검토가 필요한 작업 목록
     */
    List<TaskSubmission> getTasksForReview(Long branchId);
}