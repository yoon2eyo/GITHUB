package com.smartfitness.contracts.ports;

import com.smartfitness.contracts.model.BranchOwnerInfo;
import com.smartfitness.contracts.model.BranchOwnerRegistration;

/**
 * Branch Owner 계정 및 지점 정보 관리 인터페이스
 * UC-03: 지점주 계정 등록
 * UC-18: 지점 정보 등록
 */
public interface IBranchOwnerManagementService {
    /**
     * 지점주 계정 등록
     * @param registration 지점주 등록 정보
     * @return 등록된 지점주 정보
     */
    BranchOwnerInfo registerBranchOwner(BranchOwnerRegistration registration);

    /**
     * 지점 정보 업데이트
     * @param branchOwnerId 지점주 ID
     * @param branchInfo 지점 정보
     * @return 업데이트된 지점 정보
     */
    BranchOwnerInfo updateBranchInfo(String branchOwnerId, BranchOwnerInfo branchInfo);

    /**
     * 지점주 정보 조회
     * @param branchOwnerId 지점주 ID
     * @return 지점주 정보
     */
    BranchOwnerInfo getBranchOwnerInfo(String branchOwnerId);
}
