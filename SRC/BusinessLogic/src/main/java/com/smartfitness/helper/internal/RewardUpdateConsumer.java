package com.smartfitness.helper.internal;

import com.smartfitness.helper.ports.IHelperEventConsumer;
import com.smartfitness.helper.ports.IHelperRepository;
import com.smartfitness.event.IDomainEvent;
import com.smartfitness.event.TaskConfirmedEvent;
import org.springframework.stereotype.Component;

/**
 * 작업 확인 이벤트를 구독하여 Helper의 보상을 업데이트하는 이벤트 소비자입니다.
 */
@Component
public class RewardUpdateConsumer implements IHelperEventConsumer {
    
    private final IHelperRepository helperRepository;
    
    public RewardUpdateConsumer(IHelperRepository helperRepository) {
        this.helperRepository = helperRepository;
    }

    @Override
    public void handleEvent(IDomainEvent event) {
        if (!(event instanceof TaskConfirmedEvent)) {
            return;
        }

        TaskConfirmedEvent confirmedEvent = (TaskConfirmedEvent) event;
        
        // 작업 상태 및 점수 업데이트
        helperRepository.updateTaskStatus(
            confirmedEvent.getTaskId(),
            "COMPLETED",
            confirmedEvent.getScore()
        );
        
        // Helper 보상 잔액 업데이트
        helperRepository.updateBalance(
            confirmedEvent.getHelperId(),
            confirmedEvent.getRewardAmount()
        );
    }

    @Override
    public String getSubscriptionTopic() {
        return "tasks.confirmed";
    }
}