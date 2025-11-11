package com.smartfitness.contracts.model;

/**
 * 지점주 등록 요청 모델
 * UC-03: 지점주 계정 등록
 */
public class BranchOwnerRegistration {
    private String userId;                 // 인증 서비스 사용자 ID
    private String businessName;           // 사업체명
    private String businessRegistration;   // 사업자등록번호
    private String ownerName;              // 지점주 이름
    private String ownerPhone;             // 지점주 연락처
    private String branchName;             // 지점명
    private String branchAddress;          // 지점 주소
    private String branchPhone;            // 지점 전화

    public BranchOwnerRegistration() {
    }

    public BranchOwnerRegistration(String userId, String businessName, String businessRegistration,
                                   String ownerName, String ownerPhone, String branchName,
                                   String branchAddress, String branchPhone) {
        this.userId = userId;
        this.businessName = businessName;
        this.businessRegistration = businessRegistration;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.branchPhone = branchPhone;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "BranchOwnerRegistration{" +
                "userId='" + userId + '\'' +
                ", businessName='" + businessName + '\'' +
                ", branchName='" + branchName + '\'' +
                '}';
    }
}
