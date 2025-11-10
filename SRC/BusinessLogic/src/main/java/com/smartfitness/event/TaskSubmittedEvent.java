package com.smartfitness.event;

/**
 * TaskSubmittedEvent: Emitted when a helper task submission is recorded and
 * queued for asynchronous AI review.
 */
public class TaskSubmittedEvent implements DomainEvent {
    private final Long taskId;
    private final Long helperId;
    private final String imageUrl;

    public TaskSubmittedEvent(Long taskId, Long helperId, String imageUrl) {
        this.taskId = taskId;
        this.helperId = helperId;
        this.imageUrl = imageUrl;
    }

    public Long getTaskId() {
        return taskId;
    }

    public Long getHelperId() {
        return helperId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
