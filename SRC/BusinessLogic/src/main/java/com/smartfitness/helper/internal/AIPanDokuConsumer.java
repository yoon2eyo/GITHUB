package com.smartfitness.helper.internal;

import com.smartfitness.helper.ports.IHelperEventConsumer;
import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.helper.ports.IAIPanDokuServiceApi;
import com.smartfitness.event.IDomainEvent;
import com.smartfitness.event.TaskSubmittedEvent;
import org.springframework.stereotype.Component;

/**
 * 작업 제출 이벤트를 구독하여 AI PanDoku 검토를 요청하는 이벤트 소비자입니다.
 */
@Component
public class AIPanDokuConsumer implements IHelperEventConsumer {
    
    private final IHelperRepository helperRepository;
    private final IAIPanDokuServiceApi aiPanDokuService;
    
    public AIPanDokuConsumer(IHelperRepository helperRepository, IAIPanDokuServiceApi aiPanDokuService) {
        this.helperRepository = helperRepository;
        this.aiPanDokuService = aiPanDokuService;
    }

    @Override
    public void handleEvent(IDomainEvent event) {
        if (!(event instanceof TaskSubmittedEvent)) {
            return;
        }

        TaskSubmittedEvent taskEvent = (TaskSubmittedEvent) event;
        
        // AI PanDoku 서비스에 검토 요청
        aiPanDokuService.requestInitialPanDoku(
            taskEvent.getTaskId(),
            taskEvent.getImageUrl()
        );
        
        // 작업 상태 업데이트
        helperRepository.updateTaskStatus(
            taskEvent.getTaskId(),
            "IN_REVIEW",
            null
        );
    }

    @Override
    public String getSubscriptionTopic() {
        return "tasks.submitted";
    }
}