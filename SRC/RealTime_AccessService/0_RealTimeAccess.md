

## 💻 Real-Time Access Layer 코드 목록

### A. Access Service (AccessAuthorizationManager & GateController)

Access Service는 출입 요청을 처리하고 인증 로직을 관리하는 핵심 서비스입니다.

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **AccessAuthorizationManager** | `com.smartfitness.access.internal.logic.AccessAuthorizationManager` |
| **FaceVectorCache** | `com.smartfitness.access.internal.cache.FaceVectorCache` |
| **GateController** | `com.smartfitness.access.internal.logic.GateController` |

* **FaceVectorCache:** 
* **AccessAuthorizationManager:** 

```java
package com.smartfitness.access.internal.logic;

import com.smartfitness.access.ports.IAccessVectorRepository;
import com.smartfitness.access.ports.IFaceModelService;
import com.smartfitness.access.ports.IAccessServiceApi; // Provided Port
import com.smartfitness.access.model.AccessGrantResult;
import com.smartfitness.access.model.AccessRequest;
import com.smartfitness.common.model.FaceVector;
import com.smartfitness.messaging.ports.IMessagePublisherService; // Message Publisher
import com.smartfitness.event.AccessAttemptEvent;
import java.util.Optional;

/**
 * AccessAuthorizationManager: 출입 인증 로직을 관리하는 핵심 비즈니스 컴포넌트입니다.
 * Tactic: Separate Entities (인증 로직과 게이트 제어 로직을 분리)
 */
public class AccessAuthorizationManager implements IAccessServiceApi {
    private final IAccessVectorRepository vectorRepository;
    private final IFaceModelService modelClient;
    private final GateController gateController;
    private final IMessagePublisherService messagePublisher;

    public AccessAuthorizationManager(IAccessVectorRepository vectorRepository, 
                                      IFaceModelService modelClient, 
                                      GateController gateController,
                                      IMessagePublisherService messagePublisher) {
        this.vectorRepository = vectorRepository;
        this.modelClient = modelClient;
        this.gateController = gateController;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public AccessGrantResult requestAccessGrant(AccessRequest request) {
        // 1. 벡터 데이터 조회 (Persistence Layer)
        Optional<FaceVector> storedVectorOpt = vectorRepository.findVectorById(request.getFaceId());
        AccessGrantResult result;

        if (storedVectorOpt.isEmpty()) {
            result = AccessGrantResult.DENIED("Unregistered Face ID.");
        } else {
            // 2. 벡터 비교 (Face Model Service 호출)
            byte[] storedVector = storedVectorOpt.get().getEncryptedVector();
            double similarityScore = modelClient.compareVectors(request.getVectorData(), storedVector);

            // 3. 인증 규칙 적용
            if (similarityScore >= 0.95) { // BG-01 목표 달성 규칙
                result = AccessGrantResult.GRANTED("Access Approved.");
            } else {
                result = AccessGrantResult.DENIED("Low Similarity Score.");
            }
        }
        
        // 4. 게이트 제어 및 이벤트 발행
        gateController.controlGate(result, request.getEquipmentId());
        messagePublisher.publish(new AccessAttemptEvent(request.getFaceId(), result.isGranted()));
        
        return result;
    }
}

/**
 * GateController: 물리적 게이트 제어 및 Equipment System과의 통신을 담당합니다.
 * Tactic: Process Control (물리적 환경 변수를 제어하는 루프 구조)
 */
public class GateController {
    // 외부 Equipment System과의 통신 클라이언트(IoT Client)가 주입된다고 가정
    
    public GateController() {
        // ...
    }

    /**
     * 출입 요청 결과를 받아 물리적 게이트를 제어합니다 (UC-22).
     */
    public void controlGate(AccessGrantResult result, String equipmentId) {
        if (result.isGranted()) {
            // 게이트 개방 신호 전송 로직 (UC-22)
            System.out.println("Gate opened for: " + equipmentId);
        } else {
            // 거부 신호 전송 로직
            System.out.println("Access denied at: " + equipmentId);
        }
    }
    // ... monitorStatus() 등은 Monitoring Service의 책임으로 분리됨 ...
}
```

### B. FACE MODEL Service (VectorComparisonEngine & ModelLifecycleManager)

FACE MODEL Service는 **초저지연 벡터 비교**와 **무중단 모델 로딩**을 담당합니다.

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **VectorComparisonEngine** | `com.smartfitness.facemodel.internal.logic.VectorComparisonEngine` |
| **ModelLifecycleManager** | `com.smartfitness.facemodel.internal.logic.ModelLifecycleManager` |

* **VectorComparisonEngine:** `CompletableFuture`�� ����Ͽ� ���� ���� �밪�� ������ ����/��Ī/�����ִ� 3���� Pipeline�� ��ġ�մϴ�. (DD-05 Pipeline Optimization, Introduce Concurrency)
* **ModelLifecycleManager:** Hot Swap�� ����, MLOps Tier���� �� ���� �� ����ʿ� �����մϴ�.

```java
package com.smartfitness.facemodel.internal.logic;

import com.smartfitness.facemodel.ports.IFaceModelService;
import com.smartfitness.facemodel.ports.IModelManagementPort;
import com.smartfitness.common.model.FaceVector;
import com.smartfitness.mlo.model.LoadedModel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * VectorComparisonEngine: 안면 벡터 비교 알고리즘을 구현하는 컴포넌트입니다.
 * Tactic: Pipeline Optimization (DD-05), Introduce Concurrency
 */
public class VectorComparisonEngine implements IFaceModelService {
    // 현재 활성화된 모델 버전을 AtomicReference로 관리하여 Hot Swap을 지원합니다.
    private static final AtomicReference<LoadedModel> activeModel = new AtomicReference<>();
    
    public VectorComparisonEngine() {
        // 모델 로딩 확인 및 초기화 로직
    }

    @Override
    public double compareVectors(FaceVector requestedVector, FaceVector storedVector) {
        LoadedModel model = activeModel.get();
        if (model == null) {
            // 초기 모델 로딩 실패 시 예외 처리
            return 0.0;
        }
        
        // DD-05 Pipeline Optimization: 특징 추출과 매칭을 병렬 처리하는 파이프라인 호출
        // Tactic: Introduce Concurrency (단일 요청 Latency 최소화)
        // double score = model.executeComparisonPipeline(requestedVector, storedVector);
        
        // 실제 연산 대신 Placeholder 반환
        return 0.96; 
    }
}


/**
 * ModelLifecycleManager: MLOps로부터 모델을 받아 메모리에 로드하는 책임을 가집니다.
 * Tactic: Hot Swap (QAS-06)
 */
public class ModelLifecycleManager implements IModelManagementPort {
    // 이전 로드된 모델들을 저장하여 빠른 롤백을 지원합니다.
    private static final ConcurrentHashMap<String, LoadedModel> modelHistory = new ConcurrentHashMap<>();

    @Override
    public void loadNewModel(byte[] modelBinary) {
        // 1. 새 모델을 메모리에 로드
        LoadedModel newModel = LoadedModel.loadFromBinary(modelBinary);
        
        // 2. 현재 모델을 history에 저장 (롤백 준비)
        LoadedModel currentModel = VectorComparisonEngine.activeModel.get();
        if (currentModel != null) {
            VectorComparisonEngine.modelHistory.put(currentModel.getVersion(), currentModel);
        }
        
        // 3. Atomic Swap 실행 (Hot Swap: 무중단 교체)
        VectorComparisonEngine.activeModel.set(newModel);
    }

    @Override
    public void rollbackToPreviousModel() {
        // 이전 버전 모델을 history에서 찾아 Atomic Swap으로 복구 (BG-11 지원)
        // ... 롤백 로직 ...
    }
}
```
