package com.smartfitness.helper.model;

/**
 * TaskSubmission: Represents a helper's submitted task payload.
 */
public class TaskSubmission {
    private final Long id;
    private final Long helperId;
    private final String imageUrl;

    public TaskSubmission(Long id, Long helperId, String imageUrl) {
        this.id = id;
        this.helperId = helperId;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Long getHelperId() {
        return helperId;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
