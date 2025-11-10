네, 알겠습니다. **Business Logic Layer** 다음 티어인 **Real-Time Access Layer**는 이전 응답에서 이미 출력되었으므로, 이제 세 번째 능동적인 Layer인 \*\*AI Pipeline Layer (MLOps Tier)\*\*의 나머지 코드를 이어서 출력하겠습니다.

-----

## 💻 AI Pipeline Layer 코드 목록

AI Pipeline Layer는 **MLOPS/Training Service** 단일 노드로 구성되며, **일일 배치 학습**과 **Hot Swap 배포** 로직을 담당합니다.

### A. MLOPS/Training Service (TrainingManager & DeploymentService)

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **TrainingManager** | `com.smartfitness.mlo.internal.manager.TrainingManager` |
| **DeploymentService** | `com.smartfitness.mlo.internal.deployment.DeploymentService` |

```java
package com.smartfitness.mlo.internal.manager;

import com.smartfitness.mlo.ports.ITrainingTriggerService;
import com.smartfitness.mlo.internal.collector.DataCollector;
import com.smartfitness.mlo.internal.deployment.DeploymentService;
import com.smartfitness.mlo.internal.storage.IModelDataRepository;
import java.util.Date;
import java.util.List;

/**
 * TrainingManager: 일일 배치 학습을 관리하고 모델 검증 후 배포를 요청하는 컴포넌트입니다.
 * Tactic: Batch Sequential (일일 배치), Automated Verification
 */
public class TrainingManager implements ITrainingTriggerService {
    private final DataCollector dataCollector;
    private final DeploymentService deploymentService;
    private final IModelDataRepository modelStorage; // MLOps Tier의 자체 DB 접근 클라이언트

    public TrainingManager(DataCollector dataCollector, DeploymentService deploymentService, IModelDataRepository modelStorage) {
        this.dataCollector = dataCollector;
        this.deploymentService = deploymentService;
        this.modelStorage = modelStorage;
    }

    /**
     * 일일 배치 타이머에 의해 호출됩니다.
     */
    @Override
    public void triggerDailyBatch(Date timestamp) {
        System.out.println("MLOps: Daily batch training triggered at " + timestamp);
        
        // 1. 학습 데이터 수집 (다른 서비스의 DB에서 Read-Only 접근)
        List<byte[]> trainingData = dataCollector.collectDailyTrainingData();
        
        // 2. 모델 학습 로직 실행
        byte[] newModelBinary = executeTrainingAlgorithm(trainingData);

        // 3. 모델 검증 (BG-14: 오판독률 1.0% 미만 검증)
        if (verifyModelAccuracy(newModelBinary)) {
            String newVersion = "v" + System.currentTimeMillis();
            
            // 4. 모델 저장 (DS-05)
            modelStorage.saveModelBinary("FaceRecognition", newVersion, newModelBinary);
            
            // 5. Hot Swap 배포 요청 (QAS-06)
            deploymentService.executeZeroDowntimeDeployment("FaceRecognition", newVersion);
        } else {
            System.err.println("MLOps: New model failed verification. Deployment aborted.");
        }
    }

    private byte[] executeTrainingAlgorithm(List<byte[]> data) {
        // 실제 AI/ML 학습 로직 (e.g., Python/TensorFlow 프로세스 호출)
        return "new_model_binary_data".getBytes();
    }
    
    private boolean verifyModelAccuracy(byte[] model) {
        // 실제 검증 로직 (오판독률 < 1.0% 체크)
        return true; 
    }
}
```

```java
package com.smartfitness.mlo.internal.deployment;

import com.smartfitness.mlo.ports.IFaceModelClient; // Real-Time Tier와의 통신 클라이언트
import com.smartfitness.mlo.internal.storage.IModelDataRepository; 
import com.smartfitness.system.exception.DeploymentException;

/**
 * DeploymentService: Hot Swap 배포를 실행하고 QAS-06(무중단/신속 복구)을 책임지는 컴포넌트입니다.
 * Tactic: Hot Swap, Rollback, Automated Verification
 */
public class DeploymentService {
    private final IModelDataRepository modelStorage;
    private final IFaceModelClient faceModelClient; 

    public DeploymentService(IModelDataRepository modelStorage, IFaceModelClient faceModelClient) {
        this.modelStorage = modelStorage;
        this.faceModelClient = faceModelClient;
    }

    /**
     * 모델을 FACE MODEL Service로 무중단 배포하고, 자동 롤백 체계를 설정합니다.
     */
    public void executeZeroDowntimeDeployment(String modelId, String newVersion) {
        // Tactic: Hot Swap (QAS-06)
        try {
            // 1. 모델 바이너리 로드
            byte[] modelBinary = modelStorage.loadModelBinary(modelId, newVersion);

            // 2. Real-Time Tier의 FACE MODEL Service로 Hot Swap API 호출 
            faceModelClient.callHotSwapApi(modelId, newVersion, modelBinary);
            
            System.out.println("MLOps: Model " + newVersion + " successfully deployed via Hot Swap.");

        } catch (DeploymentException e) {
            System.err.println("Deployment failed: " + e.getMessage());
            // 3. 실패 시 Rollback 실행 (BG-11 지원)
            executeRollback(modelId, "previous_version");
        }
    }

    /**
     * 배포 실패 또는 운영 중 오류 감지 시 신속하게 롤백을 실행합니다.
     * Tactic: Rollback (BG-11: 5분 이내 복구)
     */
    private void executeRollback(String modelId, String previousVersion) {
        System.out.println("MLOps: Initiating immediate rollback to " + previousVersion);
        try {
            // FACE MODEL Service의 롤백 API 호출
            faceModelClient.callRollbackApi(modelId, previousVersion);
        } catch (Exception e) {
            // Rollback 실패는 치명적입니다. 운영팀에 즉시 알림이 가야 합니다.
            System.err.println("Rollback failed! System needs manual intervention.");
            // (실제 구현에서는 Monitoring Service로 Escalating Restart 이벤트를 발행해야 함)
        }
    }
}
```
네, 알겠습니다. \*\*AI Pipeline Layer (MLOps Tier)\*\*에 속하는 나머지 누락된 컴포넌트인 **DataCollector**와 **ModelStorage** (인터페이스 및 내부 클래스)의 코드를 이어서 출력하겠습니다.

-----

## 💻 AI Pipeline Layer 코드 목록 (Continuation)

### B. MLOPS/Training Service (DataCollector & IModelDataRepository)

이 컴포넌트들은 **Database per Service** 원칙을 준수하며, 다른 서비스의 DB로부터 **읽기 전용**으로 학습 데이터를 수집하고, \*\*MLOps의 자체 DB(DS-05)\*\*에 데이터를 저장하는 책임을 가집니다.

| 컴포넌트 | 파일 경로 |
| :--- | :--- |
| **DataCollector** | `com.smartfitness.mlo.internal.collector.DataCollector` |
| **IModelDataRepository** | `com.smartfitness.mlo.internal.storage.IModelDataRepository` |

```java
package com.smartfitness.mlo.internal.storage;

import java.util.List;
import java.util.Optional;

/**
 * IModelDataRepository: MLOps Tier의 전용 저장소인 DS-05(AI 학습 데이터셋)에 대한 내부 접근을 정의합니다.
 * Role: Handles direct CRUD operations on training data and model binaries.
 */
public interface IModelDataRepository {
    
    /**
     * DD-08: 학습에 필요한 원시 데이터(성공 인증 사진, 수정 컨펌 데이터)를 DS-05에 저장합니다.
     */
    void saveRawTrainingData(String dataType, byte[] data);

    /**
     * 일일 배치 학습을 위해 DS-05에 축적된 데이터를 로드합니다.
     */
    List<byte[]> loadAllTrainingData(); 

    /**
     * 버전별 학습된 모델 바이너리 파일을 저장합니다.
     */
    void saveModelBinary(String modelId, String modelVersion, byte[] modelBinary);
    
    /**
     * 특정 버전의 모델 바이너리 파일을 로드합니다.
     */
    Optional<byte[]> loadModelBinary(String modelId, String modelVersion);
}


// ----------------------------------------------------


package com.smartfitness.mlo.internal.collector;

import com.smartfitness.mlo.ports.IAuthRepository; // Required Port (Auth DB 접근 계약)
import com.smartfitness.mlo.ports.IHelperRepository; // Required Port (Helper DB 접근 계약)
import com.smartfitness.mlo.internal.storage.IModelDataRepository; // MLOps 자체 DB 접근 포트
import com.smartfitness.domain.model.UserAccount;
import com.smartfitness.domain.model.HelperTask;
import java.util.List;
import java.util.Date;

/**
 * DataCollector: 다른 서비스의 DB(Auth, Helper)로부터 Read-Only로 학습 데이터를 수집하고 
 * 이를 MLOps의 자체 저장소(DS-05)에 저장합니다.
 * Tactic: Batch Sequential (일일 배치 수집)
 */
public class DataCollector {
    private final IAuthRepository authClient; 
    private final IHelperRepository helperClient;
    private final IModelDataRepository modelStorage; 

    public DataCollector(IAuthRepository authClient, IHelperRepository helperClient, IModelDataRepository modelStorage) {
        this.authClient = authClient;
        this.helperClient = helperClient;
        this.modelStorage = modelStorage;
    }

    /**
     * 일일 배치 학습을 위한 모든 데이터를 수집합니다.
     * (Helper/Auth DB에 대한 Read-Only 접근은 MSA Trade-off를 반영함)
     */
    public List<byte[]> collectDailyTrainingData() {
        Date oneDayAgo = new Date(System.currentTimeMillis() - 24 * 3600 * 1000);
        
        // 1. Auth Service DB에서 신규 사용자 안면 벡터 수집
        List<UserAccount> newUsers = authClient.findRecentlyRegisteredUsers(oneDayAgo);
        
        // 2. Helper Service DB에서 지점주 컨펌 완료된 작업 (정답 데이터) 수집
        List<HelperTask> groundTruthTasks = helperClient.findConfirmedTasksForRetraining();
        
        // 3. 수집된 데이터를 MLOps Tier의 DS-05에 저장 (IModelDataRepository 사용)
        saveCollectedData(newUsers, groundTruthTasks);
        
        // 4. DS-05에 저장된 모든 학습 데이터를 로드하여 TrainingManager로 반환
        return modelStorage.loadAllTrainingData();
    }
    
    private void saveCollectedData(List<UserAccount> newUsers, List<HelperTask> groundTruthTasks) {
        // 실제 저장 로직: UserAccount에서 FaceVector를 추출하거나, HelperTask에서 이미지를 참조하여 저장
        // ...
        
        modelStorage.saveRawTrainingData("FaceVector", null); // 실제 데이터 변환 및 저장
        modelStorage.saveRawTrainingData("GroundTruth", null);
    }
}
```