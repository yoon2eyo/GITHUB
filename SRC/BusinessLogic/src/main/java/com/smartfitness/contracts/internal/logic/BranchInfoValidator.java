package com.smartfitness.contracts.internal.logic;

import com.smartfitness.contracts.model.BranchInfo;
import com.smartfitness.contracts.ports.IBranchInfoService;
import com.smartfitness.contracts.ports.IBranchRepository;

/**
 * 지점 정보 검증 및 조회 구현
 * UC-19: 고객 리뷰 조회
 */
public class BranchInfoValidator implements IBranchInfoService {
    private final IBranchRepository branchRepository;

    public BranchInfoValidator(IBranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public BranchInfo getBranchInfo(String branchId) {
        // 지점 정보 조회는 BranchOwnerInfo에서 추출
        // 실제 구현에서는 BranchDatabase에서 조회하거나
        // BranchOwnerInfo를 BranchInfo로 변환
        return branchRepository.findById(branchId)
            .map(this::convertToBranchInfo)
            .orElseThrow(() -> new IllegalArgumentException("Branch not found: " + branchId));
    }

    @Override
    public boolean existsBranch(String branchId) {
        return branchRepository.findById(branchId).isPresent();
    }

    @Override
    public boolean validateBranchInfo(BranchInfo branchInfo) {
        // 지점 정보 유효성 검증
        if (branchInfo == null) {
            return false;
        }

        // 필수 필드 검증
        if (branchInfo.getBranchName() == null || branchInfo.getBranchName().isEmpty()) {
            return false;
        }

        if (branchInfo.getBranchAddress() == null || branchInfo.getBranchAddress().isEmpty()) {
            return false;
        }

        if (branchInfo.getBranchPhone() == null || branchInfo.getBranchPhone().isEmpty()) {
            return false;
        }

        // 연락처 형식 검증 (간단한 정규표현식)
        if (!branchInfo.getBranchPhone().matches("\\d{2,3}-\\d{3,4}-\\d{4}")) {
            return false;
        }

        return true;
    }

    /**
     * BranchOwnerInfo를 BranchInfo로 변환
     */
    private BranchInfo convertToBranchInfo(com.smartfitness.contracts.model.BranchOwnerInfo ownerInfo) {
        BranchInfo branchInfo = new BranchInfo();
        branchInfo.setBranchId(ownerInfo.getBranchOwnerId());
        branchInfo.setBranchOwnerId(ownerInfo.getBranchOwnerId());
        branchInfo.setBranchName(ownerInfo.getBranchName());
        branchInfo.setBranchAddress(ownerInfo.getBranchAddress());
        branchInfo.setBranchPhone(ownerInfo.getBranchPhone());
        branchInfo.setBranchArea(ownerInfo.getBranchArea());
        branchInfo.setEquipmentCount(ownerInfo.getEquipmentCount());
        branchInfo.setOperatingStatus(ownerInfo.getOperatingStatus());
        return branchInfo;
    }
}
