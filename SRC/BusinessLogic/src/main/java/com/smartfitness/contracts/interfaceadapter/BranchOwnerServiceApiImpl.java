package com.smartfitness.contracts.interfaceadapter;

import com.smartfitness.contracts.model.BranchOwnerInfo;
import com.smartfitness.contracts.model.BranchInfo;
import com.smartfitness.contracts.model.BranchOwnerRegistration;
import com.smartfitness.contracts.ports.IBranchOwnerServiceApi;
import com.smartfitness.contracts.ports.IBranchOwnerManagementService;
import com.smartfitness.contracts.ports.IBranchInfoService;

/**
 * Branch Owner Service API 구현
 * HTTP 요청을 받아 비즈니스 로직으로 전달
 */
public class BranchOwnerServiceApiImpl implements IBranchOwnerServiceApi {
    private final IBranchOwnerManagementService branchOwnerManagementService;
    private final IBranchInfoService branchInfoService;

    public BranchOwnerServiceApiImpl(IBranchOwnerManagementService branchOwnerManagementService,
                                   IBranchInfoService branchInfoService) {
        this.branchOwnerManagementService = branchOwnerManagementService;
        this.branchInfoService = branchInfoService;
    }

    @Override
    public BranchOwnerInfo registerBranchOwner(BranchOwnerRegistration registration) {
        // UC-03: 지점주 계정 등록
        if (registration == null) {
            throw new IllegalArgumentException("Registration information cannot be null");
        }

        validateRegistration(registration);
        return branchOwnerManagementService.registerBranchOwner(registration);
    }

    @Override
    public BranchOwnerInfo updateBranchInfo(String branchOwnerId, BranchOwnerInfo branchInfo) {
        // UC-18: 지점 정보 등록/수정
        if (branchOwnerId == null || branchOwnerId.isEmpty()) {
            throw new IllegalArgumentException("Branch owner ID cannot be null or empty");
        }

        if (branchInfo == null) {
            throw new IllegalArgumentException("Branch info cannot be null");
        }

        if (branchInfo.getBranchName() == null || branchInfo.getBranchName().isEmpty()) {
            throw new IllegalArgumentException("Branch name is required");
        }

        if (branchInfo.getBranchAddress() == null || branchInfo.getBranchAddress().isEmpty()) {
            throw new IllegalArgumentException("Branch address is required");
        }

        if (branchInfo.getBranchPhone() == null || branchInfo.getBranchPhone().isEmpty()) {
            throw new IllegalArgumentException("Branch phone is required");
        }

        return branchOwnerManagementService.updateBranchInfo(branchOwnerId, branchInfo);
    }

    @Override
    public BranchOwnerInfo getBranchOwnerInfo(String branchOwnerId) {
        if (branchOwnerId == null || branchOwnerId.isEmpty()) {
            throw new IllegalArgumentException("Branch owner ID cannot be null or empty");
        }

        return branchOwnerManagementService.getBranchOwnerInfo(branchOwnerId);
    }

    @Override
    public BranchInfo getBranchInfo(String branchId) {
        // UC-19: 고객 리뷰 조회 (지점 정보 포함)
        if (branchId == null || branchId.isEmpty()) {
            throw new IllegalArgumentException("Branch ID cannot be null or empty");
        }

        return branchInfoService.getBranchInfo(branchId);
    }

    /**
     * 등록 정보 유효성 검증
     */
    private void validateRegistration(BranchOwnerRegistration registration) {
        if (registration.getUserId() == null || registration.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (registration.getBusinessName() == null || registration.getBusinessName().isEmpty()) {
            throw new IllegalArgumentException("Business name is required");
        }

        if (registration.getBusinessRegistration() == null || registration.getBusinessRegistration().isEmpty()) {
            throw new IllegalArgumentException("Business registration number is required");
        }

        if (registration.getOwnerName() == null || registration.getOwnerName().isEmpty()) {
            throw new IllegalArgumentException("Owner name is required");
        }

        if (registration.getOwnerPhone() == null || registration.getOwnerPhone().isEmpty()) {
            throw new IllegalArgumentException("Owner phone is required");
        }

        if (registration.getBranchName() == null || registration.getBranchName().isEmpty()) {
            throw new IllegalArgumentException("Branch name is required");
        }

        if (registration.getBranchAddress() == null || registration.getBranchAddress().isEmpty()) {
            throw new IllegalArgumentException("Branch address is required");
        }

        if (registration.getBranchPhone() == null || registration.getBranchPhone().isEmpty()) {
            throw new IllegalArgumentException("Branch phone is required");
        }
    }
}
