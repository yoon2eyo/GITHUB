package com.smartfitness.helper.interfaceadapter;

import com.smartfitness.helper.IHelperServiceApi;
import com.smartfitness.helper.ITaskManagementService;
import com.smartfitness.helper.IRewardConfirmationService;
import com.smartfitness.helper.model.TaskSubmission;
import com.smartfitness.helper.model.TaskRegistrationResult;
import com.smartfitness.helper.model.HelperBalance;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Helper 서비스의 REST API 구현체입니다.
 * 작업 제출과 보상 관리 관련 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/helper")
public class HelperTaskManagementApi implements IHelperServiceApi {

    private final ITaskManagementService taskManagementService;
    private final IRewardConfirmationService rewardConfirmationService;

    public HelperTaskManagementApi(
            ITaskManagementService taskManagementService,
            IRewardConfirmationService rewardConfirmationService) {
        this.taskManagementService = taskManagementService;
        this.rewardConfirmationService = rewardConfirmationService;
    }

    /**
     * Helper가 수행한 작업을 제출합니다.
     */
    @PostMapping("/tasks")
    @Override
    public TaskRegistrationResult registerTaskSubmission(@RequestBody TaskSubmission submission) {
        return taskManagementService.registerTaskSubmission(submission);
    }

    /**
     * Helper의 현재 보상 잔액을 조회합니다.
     */
    @GetMapping("/balance/{helperId}")
    @Override
    public Optional<HelperBalance> getHelperBalance(@PathVariable Long helperId) {
        return rewardConfirmationService.getHelperBalance(helperId);
    }

    /**
     * 특정 지점의 검토가 필요한 작업 목록을 조회합니다.
     */
    @GetMapping("/tasks/review/{branchId}")
    @Override
    public List<TaskSubmission> getTasksForReview(@PathVariable Long branchId) {
        return taskManagementService.getTasksForReview(branchId);
    }
}