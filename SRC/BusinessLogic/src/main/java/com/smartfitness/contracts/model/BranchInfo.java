package com.smartfitness.contracts.model;

/**
 * 지점 정보 모델
 * UC-19: 고객 리뷰 조회 시 지점 정보 포함
 */
public class BranchInfo {
    private String branchId;
    private String branchOwnerId;
    private String branchName;
    private String branchAddress;
    private String branchPhone;
    private String branchArea;
    private Integer equipmentCount;
    private Double averageRating;          // 평균 평점 (고객 리뷰 기반)
    private Integer reviewCount;           // 리뷰 수
    private String operatingStatus;

    public BranchInfo() {
    }

    public BranchInfo(String branchId, String branchName, String branchAddress,
                     String branchPhone, String branchArea) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.branchPhone = branchPhone;
        this.branchArea = branchArea;
    }

    // Getters and Setters
    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBranchOwnerId() {
        return branchOwnerId;
    }

    public void setBranchOwnerId(String branchOwnerId) {
        this.branchOwnerId = branchOwnerId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchAddress() {
        return branchAddress;
    }

    public void setBranchAddress(String branchAddress) {
        this.branchAddress = branchAddress;
    }

    public String getBranchPhone() {
        return branchPhone;
    }

    public void setBranchPhone(String branchPhone) {
        this.branchPhone = branchPhone;
    }

    public String getBranchArea() {
        return branchArea;
    }

    public void setBranchArea(String branchArea) {
        this.branchArea = branchArea;
    }

    public Integer getEquipmentCount() {
        return equipmentCount;
    }

    public void setEquipmentCount(Integer equipmentCount) {
        this.equipmentCount = equipmentCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    @Override
    public String toString() {
        return "BranchInfo{" +
                "branchId='" + branchId + '\'' +
                ", branchName='" + branchName + '\'' +
                ", averageRating=" + averageRating +
                ", reviewCount=" + reviewCount +
                '}';
    }
}
