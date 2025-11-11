package com.smartfitness.contracts.internal.logic;

import com.smartfitness.contracts.model.BranchOwnerInfo;
import com.smartfitness.contracts.model.BranchOwnerRegistration;
import com.smartfitness.contracts.ports.IBranchOwnerManagementService;
import com.smartfitness.contracts.ports.IBranchRepository;
import com.smartfitness.contracts.ports.IAuthRepository;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import com.smartfitness.event.IDomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 지점주 계정 및 지점 정보 관리 구현
 * UC-03: 지점주 계정 등록
 * UC-18: 지점 정보 등록
 */
public class BranchOwnerManager implements IBranchOwnerManagementService {
    private final IBranchRepository branchRepository;
    private final IAuthRepository authRepository;
    private final IMessagePublisherService messagePublisher;

    public BranchOwnerManager(IBranchRepository branchRepository,
                            IAuthRepository authRepository,
                            IMessagePublisherService messagePublisher) {
        this.branchRepository = branchRepository;
        this.authRepository = authRepository;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public BranchOwnerInfo registerBranchOwner(BranchOwnerRegistration registration) {
        // UC-03: 지점주 계정 등록
        
        // 1. 인증 서비스에서 사용자 존재 확인
        if (!authRepository.existsUser(registration.getUserId())) {
            throw new IllegalArgumentException("User not found: " + registration.getUserId());
        }

        // 2. 지점주 정보 생성
        String branchOwnerId = UUID.randomUUID().toString();
        BranchOwnerInfo branchOwnerInfo = new BranchOwnerInfo(
            branchOwnerId,
            registration.getUserId(),
            registration.getBusinessName(),
            registration.getBusinessRegistration(),
            registration.getOwnerName(),
            registration.getOwnerPhone(),
            registration.getBranchName(),
            registration.getBranchAddress(),
            registration.getBranchPhone()
        );

        // 3. 저장소에 저장
        branchRepository.save(branchOwnerInfo);

        // 4. 이벤트 발행
        publishBranchOwnerCreatedEvent(branchOwnerInfo);

        return branchOwnerInfo;
    }

    @Override
    public BranchOwnerInfo updateBranchInfo(String branchOwnerId, BranchOwnerInfo branchInfo) {
        // UC-18: 지점 정보 등록/수정
        
        // 1. 기존 지점주 정보 조회
        BranchOwnerInfo existingInfo = branchRepository.findById(branchOwnerId)
            .orElseThrow(() -> new IllegalArgumentException("Branch owner not found: " + branchOwnerId));

        // 2. 정보 업데이트
        existingInfo.setBranchName(branchInfo.getBranchName());
        existingInfo.setBranchAddress(branchInfo.getBranchAddress());
        existingInfo.setBranchPhone(branchInfo.getBranchPhone());
        existingInfo.setBranchArea(branchInfo.getBranchArea());
        existingInfo.setEquipmentCount(branchInfo.getEquipmentCount());
        existingInfo.setUpdatedAt(LocalDateTime.now());

        // 3. 저장소 업데이트
        branchRepository.update(branchOwnerId, existingInfo);

        // 4. 이벤트 발행
        publishBranchInfoUpdatedEvent(existingInfo);

        return existingInfo;
    }

    @Override
    public BranchOwnerInfo getBranchOwnerInfo(String branchOwnerId) {
        return branchRepository.findById(branchOwnerId)
            .orElseThrow(() -> new IllegalArgumentException("Branch owner not found: " + branchOwnerId));
    }

    /**
     * 지점주 생성 이벤트 발행
     */
    private void publishBranchOwnerCreatedEvent(BranchOwnerInfo branchOwnerInfo) {
        try {
            messagePublisher.publish("branch.owner.created", 
                (IDomainEvent) () -> branchOwnerInfo.getBranchOwnerId());
        } catch (Exception e) {
            // 이벤트 발행 실패는 로깅하지만 기능 진행
            System.err.println("Failed to publish branch owner created event: " + e.getMessage());
        }
    }

    /**
     * 지점 정보 업데이트 이벤트 발행
     */
    private void publishBranchInfoUpdatedEvent(BranchOwnerInfo branchOwnerInfo) {
        try {
            messagePublisher.publish("branch.info.updated", 
                (IDomainEvent) () -> branchOwnerInfo.getBranchOwnerId());
        } catch (Exception e) {
            // 이벤트 발행 실패는 로깅하지만 기능 진행
            System.err.println("Failed to publish branch info updated event: " + e.getMessage());
        }
    }
}
