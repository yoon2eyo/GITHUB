package com.smartfitness.contracts.model;

import java.time.LocalDateTime;

/**
 * 지점주 정보 모델
 * UC-03, UC-18과 관련된 지점주 및 지점 정보 저장
 */
public class BranchOwnerInfo {
    private String branchOwnerId;
    private String userId;                 // 인증 서비스의 사용자 ID
    private String businessName;           // 사업체명
    private String businessRegistration;   // 사업자등록번호
    private String ownerName;              // 지점주 이름
    private String ownerPhone;             // 지점주 연락처
    private String branchName;             // 지점명
    private String branchAddress;          // 지점 주소
    private String branchPhone;            // 지점 전화
    private String branchArea;             // 지점 면적 (평)
    private Integer equipmentCount;        // 장비 수
    private String operatingStatus;        // 운영 상태 (ACTIVE, INACTIVE, SUSPENDED)
    private LocalDateTime registeredAt;    // 등록 시간
    private LocalDateTime updatedAt;       // 최종 수정 시간

    public BranchOwnerInfo() {
    }

    public BranchOwnerInfo(String branchOwnerId, String userId, String businessName,
                          String businessRegistration, String ownerName, String ownerPhone,
                          String branchName, String branchAddress, String branchPhone) {
        this.branchOwnerId = branchOwnerId;
        this.userId = userId;
        this.businessName = businessName;
        this.businessRegistration = businessRegistration;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.branchPhone = branchPhone;
        this.operatingStatus = "ACTIVE";
        this.registeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getBranchOwnerId() {
        return branchOwnerId;
    }

    public void setBranchOwnerId(String branchOwnerId) {
        this.branchOwnerId = branchOwnerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getBusinessRegistration() {
        return businessRegistration;
    }

    public void setBusinessRegistration(String businessRegistration) {
        this.businessRegistration = businessRegistration;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
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

    public String getOperatingStatus() {
        return operatingStatus;
    }

    public void setOperatingStatus(String operatingStatus) {
        this.operatingStatus = operatingStatus;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "BranchOwnerInfo{" +
                "branchOwnerId='" + branchOwnerId + '\'' +
                ", userId='" + userId + '\'' +
                ", businessName='" + businessName + '\'' +
                ", branchName='" + branchName + '\'' +
                ", operatingStatus='" + operatingStatus + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }
}
