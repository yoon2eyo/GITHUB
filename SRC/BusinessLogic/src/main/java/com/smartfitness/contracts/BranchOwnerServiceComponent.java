package com.smartfitness.contracts;

import com.smartfitness.contracts.internal.BranchEventProcessor;
import com.smartfitness.contracts.internal.logic.BranchInfoValidator;
import com.smartfitness.contracts.internal.logic.BranchOwnerManager;
import com.smartfitness.contracts.ports.*;
import com.smartfitness.messaging.ports.IMessagePublisherService;
import com.smartfitness.messaging.ports.IMessageSubscriptionService;

import java.util.Objects;

/**
 * Branch Owner Service Composition Root
 * 모든 의존성을 주입하고 서비스를 구성
 * UC-03: 지점주 계정 등록
 * UC-18: 지점 정보 등록
 * UC-19: 고객 리뷰 조회
 */
public final class BranchOwnerServiceComponent implements AutoCloseable {
    private final BranchOwnerManager branchOwnerManager;
    private final BranchInfoValidator branchInfoValidator;
    private final BranchEventProcessor branchEventProcessor;

    private BranchOwnerServiceComponent(BranchOwnerManager branchOwnerManager,
                                       BranchInfoValidator branchInfoValidator,
                                       BranchEventProcessor branchEventProcessor) {
        this.branchOwnerManager = branchOwnerManager;
        this.branchInfoValidator = branchInfoValidator;
        this.branchEventProcessor = branchEventProcessor;
    }

    /**
     * Branch Owner Service 부트스트랩
     * @param branchRepository 지점주 저장소
     * @param authRepository 인증 저장소 (읽기 전용)
     * @param messagePublisher 메시지 발행 서비스
     * @param subscriptionService 메시지 구독 서비스
     * @return BranchOwnerServiceComponent 인스턴스
     */
    public static BranchOwnerServiceComponent bootstrap(
        IBranchRepository branchRepository,
        IAuthRepository authRepository,
        IMessagePublisherService messagePublisher,
        IMessageSubscriptionService subscriptionService) {

        Objects.requireNonNull(branchRepository, "branchRepository");
        Objects.requireNonNull(authRepository, "authRepository");
        Objects.requireNonNull(messagePublisher, "messagePublisher");
        Objects.requireNonNull(subscriptionService, "subscriptionService");

        // 비즈니스 로직 컴포넌트 생성
        BranchOwnerManager branchOwnerManager = 
            new BranchOwnerManager(branchRepository, authRepository, messagePublisher);

        BranchInfoValidator branchInfoValidator = 
            new BranchInfoValidator(branchRepository);

        // 이벤트 처리 컴포넌트 생성 및 등록
        BranchEventProcessor branchEventProcessor = 
            new BranchEventProcessor(subscriptionService);
        branchEventProcessor.register();

        return new BranchOwnerServiceComponent(
            branchOwnerManager,
            branchInfoValidator,
            branchEventProcessor
        );
    }

    /**
     * 지점주 관리 서비스 조회
     */
    public IBranchOwnerManagementService getBranchOwnerManagementService() {
        return branchOwnerManager;
    }

    /**
     * 지점 정보 서비스 조회
     */
    public IBranchInfoService getBranchInfoService() {
        return branchInfoValidator;
    }

    /**
     * 이벤트 컨슈머 조회
     */
    public IBranchEventConsumer getBranchEventConsumer() {
        return branchEventProcessor;
    }

    @Override
    public void close() throws Exception {
        // 리소스 정리
        if (branchEventProcessor != null) {
            branchEventProcessor.unregister();
        }
    }
}
