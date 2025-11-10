package com.smartfitness.helper;

import com.smartfitness.helper.model.TaskSubmission;
import com.smartfitness.helper.model.TaskRegistrationResult;
import java.util.List;

/**
 * 작업 관리를 담당하는 서비스 인터페이스입니다.
 */
public interface ITaskManagementService {
    /**
     * 새로운 작업을 등록합니다.
     */
    TaskRegistrationResult registerTaskSubmission(TaskSubmission submission);

    /**
     * 검토가 필요한 작업 목록을 조회합니다.
     */
    List<TaskSubmission> getTasksForReview(Long branchId);
}