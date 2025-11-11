package com.smartfitness.contracts.ports;

import com.smartfitness.contracts.model.BranchInfo;

/**
 * 지점 정보 조회 및 검증 인터페이스
 * UC-19: 고객 리뷰 조회
 */
public interface IBranchInfoService {
    /**
     * 지점 정보 조회
     * @param branchId 지점 ID
     * @return 지점 정보
     */
    BranchInfo getBranchInfo(String branchId);

    /**
     * 지점 정보 존재 여부 확인
     * @param branchId 지점 ID
     * @return 존재 여부
     */
    boolean existsBranch(String branchId);

    /**
     * 지점 정보 유효성 검증
     * @param branchInfo 지점 정보
     * @return 검증 성공 여부
     */
    boolean validateBranchInfo(BranchInfo branchInfo);
}
