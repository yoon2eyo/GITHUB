package com.smartfitness.facemodel.system;

import com.smartfitness.facemodel.interfaceadapter.FaceModelServiceApiImpl;
import com.smartfitness.facemodel.internal.VectorComparisonEngine;
import com.smartfitness.facemodel.internal.ModelLifecycleManager;
import com.smartfitness.facemodel.internal.FaceModelEventProcessor;
import com.smartfitness.facemodel.ports.IFaceModelServiceApi;

/**
 * 얼굴 모델 서비스 구성 루트
 * Dependency Injection 및 컴포넌트 조합
 */
public class FaceModelServiceComponent {
    private static FaceModelServiceComponent instance;
    private final VectorComparisonEngine vectorComparisonEngine;
    private final ModelLifecycleManager modelLifecycleManager;
    private final FaceModelEventProcessor faceModelEventProcessor;
    private final IFaceModelServiceApi faceModelServiceApi;

    private FaceModelServiceComponent() {
        // 1. 내부 컴포넌트 생성
        this.vectorComparisonEngine = new VectorComparisonEngine();
        this.modelLifecycleManager = new ModelLifecycleManager();
        this.faceModelEventProcessor = new FaceModelEventProcessor();

        // 2. API 구현체 생성 (DI)
        this.faceModelServiceApi = new FaceModelServiceApiImpl(
            vectorComparisonEngine,
            modelLifecycleManager
        );

        // 3. 이벤트 핸들러 등록
        this.faceModelEventProcessor.register();
    }

    /**
     * 싱글톤 인스턴스 조회
     */
    public static synchronized FaceModelServiceComponent getInstance() {
        if (instance == null) {
            instance = new FaceModelServiceComponent();
        }
        return instance;
    }

    /**
     * Face Model Service API 조회
     */
    public IFaceModelServiceApi getFaceModelServiceApi() {
        return faceModelServiceApi;
    }

    /**
     * Vector Comparison Engine 조회
     */
    public VectorComparisonEngine getVectorComparisonEngine() {
        return vectorComparisonEngine;
    }

    /**
     * Model Lifecycle Manager 조회
     */
    public ModelLifecycleManager getModelLifecycleManager() {
        return modelLifecycleManager;
    }

    /**
     * Face Model Event Processor 조회
     */
    public FaceModelEventProcessor getFaceModelEventProcessor() {
        return faceModelEventProcessor;
    }

    /**
     * 서비스 종료 (리소스 정리)
     */
    public void shutdown() {
        if (faceModelEventProcessor != null) {
            faceModelEventProcessor.unregister();
        }
    }
}
