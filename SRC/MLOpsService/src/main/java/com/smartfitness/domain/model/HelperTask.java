package com.smartfitness.domain.model;

public class HelperTask {
    private String taskId;
    private String imageId;
    private String confirmedLabel;
    
    // Getters and setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getImageId() {
        return imageId;
    }
    
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
    
    public String getConfirmedLabel() {
        return confirmedLabel;
    }
    
    public void setConfirmedLabel(String confirmedLabel) {
        this.confirmedLabel = confirmedLabel;
    }
}