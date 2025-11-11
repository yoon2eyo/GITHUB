package com.smartfitness.contracts.ports;

import com.smartfitness.domain.model.UserAccount;
import java.util.Optional;

/**
 * 인증 서비스 레포지토리 인터페이스
 * 지점주 계정 정보 조회 (읽기 전용)
 */
public interface IAuthRepository {
    /**
     * 사용자 계정 조회
     * @param userId 사용자 ID
     * @return 사용자 계정 (Optional)
     */
    Optional<UserAccount> findUserById(String userId);

    /**
     * 이메일로 사용자 조회
     * @param email 이메일
     * @return 사용자 계정 (Optional)
     */
    Optional<UserAccount> findUserByEmail(String email);

    /**
     * 사용자 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsUser(String userId);
}
