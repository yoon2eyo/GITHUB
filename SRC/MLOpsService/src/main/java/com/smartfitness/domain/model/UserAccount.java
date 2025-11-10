package com.smartfitness.domain.model;

public class UserAccount {
    private String userId;
    private byte[] faceVector;
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public byte[] getFaceVector() {
        return faceVector;
    }
    
    public void setFaceVector(byte[] faceVector) {
        this.faceVector = faceVector;
    }
}