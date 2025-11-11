package com.smartfitness.contracts.ports;

import com.smartfitness.contracts.model.BranchOwnerInfo;
import com.smartfitness.contracts.model.BranchOwnerRegistration;

/**
 * Branch Owner Service API 인터페이스
 * 외부 요청을 처리하는 진입점
 */
public interface IBranchOwnerServiceApi {
    /**
     * 지점주 계정 등록 (UC-03)
     * @param registration 지점주 등록 정보
     * @return 등록된 지점주 정보
     */
    BranchOwnerInfo registerBranchOwner(BranchOwnerRegistration registration);

    /**
     * 지점 정보 등록/수정 (UC-18)
     * @param branchOwnerId 지점주 ID
     * @param branchInfo 지점 정보
     * @return 업데이트된 지점주 정보
     */
    BranchOwnerInfo updateBranchInfo(String branchOwnerId, BranchOwnerInfo branchInfo);

    /**
     * 지점주 정보 조회
     * @param branchOwnerId 지점주 ID
     * @return 지점주 정보
     */
    BranchOwnerInfo getBranchOwnerInfo(String branchOwnerId);

    /**
     * 지점 정보 조회 (UC-19)
     * @param branchId 지점 ID
     * @return 지점 정보
     */
    com.smartfitness.contracts.model.BranchInfo getBranchInfo(String branchId);
}
