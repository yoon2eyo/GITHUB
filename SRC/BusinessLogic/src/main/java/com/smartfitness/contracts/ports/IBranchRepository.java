package com.smartfitness.contracts.ports;

import com.smartfitness.contracts.model.BranchOwnerInfo;
import java.util.Optional;

/**
 * 지점주 정보 저장소 인터페이스
 */
public interface IBranchRepository {
    /**
     * 지점주 정보 저장
     * @param branchOwnerInfo 지점주 정보
     */
    void save(BranchOwnerInfo branchOwnerInfo);

    /**
     * 지점주 정보 조회
     * @param branchOwnerId 지점주 ID
     * @return 지점주 정보 (Optional)
     */
    Optional<BranchOwnerInfo> findById(String branchOwnerId);

    /**
     * 지점주 이메일로 조회
     * @param email 이메일
     * @return 지점주 정보 (Optional)
     */
    Optional<BranchOwnerInfo> findByEmail(String email);

    /**
     * 지점주 정보 업데이트
     * @param branchOwnerId 지점주 ID
     * @param branchOwnerInfo 업데이트된 지점주 정보
     */
    void update(String branchOwnerId, BranchOwnerInfo branchOwnerInfo);

    /**
     * 지점주 정보 삭제
     * @param branchOwnerId 지점주 ID
     */
    void delete(String branchOwnerId);
}
