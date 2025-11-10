package com.smartfitness.event;

/**
 * TaskConfirmedEvent: Emitted when an administrator completes a review cycle and
 * finalizes the reward decision for a helper task.
 */
public class TaskConfirmedEvent implements DomainEvent {
    private final Long taskId;
    private final Long helperId;
    private final double rewardAmount;
    private final boolean approved;

    public TaskConfirmedEvent(Long taskId, Long helperId, double rewardAmount, boolean approved) {
        this.taskId = taskId;
        this.helperId = helperId;
        this.rewardAmount = rewardAmount;
        this.approved = approved;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getHelperId() {
        return helperId;
    }

    public double getRewardAmount() {
        return rewardAmount;
    }

    public boolean isApproved() {
        return approved;
    }
}
