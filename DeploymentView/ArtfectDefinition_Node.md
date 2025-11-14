4.3.2.	Node Specification

Name	Description
N-01
Load Balance	역할: 외부 클라이언트 요청을 API Gateway로 분산하는 L7 로드 밸런서.
HTTPS 요청 수신 → TLS 복호화 → 헬스체크로 정상 API Gateway 인스턴스 선택 → HTTP로 전달
Multiplicity: 2+ (Active-Active HA 구성)
QA 기여: 장애 전파 차단 및 자동 복구 (QAS-05), 트래픽 분산 최적화, 보안 강화 (QAS-04)
N-02
API Gateway	역할: 모든 클라이언트 요청의 단일 진입점, 인증/인가,라우팅, Circuit Breaker 수행. 
요청 수신 → JWT 검증 → Rate Limit 체크 → Eureka에서 대상 서비스 인스턴스 조회 → HTTP 라우팅 → 응답 반환
Multiplicity: 3 replicas
QA 기여: 장애 전파 차단 및 자동 복구 (QAS-05), 보안 강화 (QAS-04), 성능 최적화
N-03
RealTime Access	역할: Access Service와 FaceModel Service가 동일 Pod에 공존하여 초저지연 출입 인증 수행
Multiplicity: 2 replicas
QA 기여: 성능 최적화 (QAS-02), 가용성 보장
N-04
Auth Service	역할: 사용자 인증/인가, 회원가입,
JWT 발급 수행. 회원가입 요청 → 신용카드 검증 (외부 API) → 안면 사진 S3 업로드 → 특징점 추출 → 벡터 DB 저장 → JWT 발급
Multiplicity: 2 replicas
QA 기여: 보안 강화 (QAS-04), 가용성 보장
N-05
Helper Service	역할: 헬퍼 작업 등록, AI 판독 요청, 보상 관리 수행. 
작업 사진 업로드 → S3 저장 → TaskSubmittedEvent 발행 → RabbitMQ → AI 분석 대기
Multiplicity: 2 replicas
QA 기여: 수정 용이성 (QAS-06), 가용성 보장
N-06
Search Service	역할: 자연어 지점 검색 (Hot Path), 콘텐츠 인덱싱 (Cold Path) 수행. 
Hot Path: 검색 요청 → Simple Tokenizer (키워드 추출) → ElasticSearch 쿼리 → 결과 반환. 
Cold Path: 콘텐츠 등록 이벤트 수신 → LLM API 호출 (비동기) → 키워드 추출 → ElasticSearch 인덱싱
Multiplicity: 3 replicas (높은 검색 트래픽 대비)
QA 기여: 성능 최적화 (QAS-03), 확장성

N-07
BranchOwner Service	역할: 지점 정보 관리, 작업 검수/컨펌 수행. 작업 검수 요청 → AI 판독 결과 표시 → 지점주 컨펌 → TaskConfirmedEvent 발행 → RabbitMQ
Multiplicity: 2 replicas
QA 기여: 수정 용이성 (QAS-06), 가용성 보장
N-08
Monitoring Service	역할: 설비 상태 모니터링, 장애 감지 (Heartbeat + Ping/Echo) 수행. 
Quartz Scheduler가 10초마다 트리거 → 모든 설비 최근 Heartbeat 확인 → 30초 초과 시 Ping 전송 → 무응답 시 EquipmentFaultEvent 발행
Multiplicity: 2 replicas
QA 기여: 고장 감지 및 알림 (QAS-01), 가용성 보장
N-09
Notification Service	역할: 이벤트 기반 푸시 알림 발송 (FCM). RabbitMQ에서 이벤트 수신 → 지점주 디바이스 토큰 조회 → FCM API 호출 → Push 전송
Multiplicity: 3 replicas (높은 알림 부하 대비)
QA 기여: 고장 감지 및 알림 (QAS-01), 확장성
N-10
MLOps Service	역할: AI 모델 재학습, 검증, 배포 (Hot Swap) 수행. 훈련 인스턴스: 재학습 파이프라인 실행 (데이터 수집 → 전처리 → 훈련 → 검증 → 배포). Production 인스턴스: gRPC 서버로 FaceModel Service에 모델 배포 요청 처리
Multiplicity: 1-2 replicas (1: Production, 1: Training 전용)
QA 기여: 수정 용이성 (QAS-06), 성능 최적화, 비용 효율성
N-11
RDS Cluster	역할: 각 서비스별 독립 PostgreSQL 데이터베이스 클러스터.
DB 목록: Auth DB, Helper DB, Search DB, Branch DB, Monitor DB, Vector DB, Model DB, Training Data DB
Multiplicity: 8
QA 기여: 장애 전파 차단 및 자동 복구 (QAS-05), 성능 최적화, 보안 강화 (QAS-04), 수정 용이성 (DD-03)
N-12
ElasticSearch Cluster	역할: 전문 검색 엔진, 지점/리뷰 Full-text Search 제공. 
Master Node: 클러스터 상태 관리, 샤드 할당, 인덱스 생성/삭제. 
Multiplicity: 3 (1 Master, 2 Data nodes)
QA 기여: 성능 최적화 (QAS-03), 가용성 보장, 확장성
N-13
RabbitMQ Cluster	역할: 메시지 브로커, 이벤트 기반 비동기 통신 중개. 
Multiplicity: 3 nodes (Quorum Queue)
QA 기여: 가용성 보장 (DD-02), 수정 용이성, 성능 최적화
N-14
Redis Cache Cluster	역할: 인메모리 캐시, Face Vector Cache 및 Session 저장.
Multiplicity: 3 (1 Primary, 2 Replicas)
QA 기여: 성능 최적화 (QAS-02), 가용성 보장
N-15
S3 Storage	역할: 객체 스토리지, 작업 사진 및 안면 이미지 저장.
Helper/Auth/MLOps Service가 사진 업로드 → S3 저장
Multiplicity: *  (관리형 서비스, 무제한 확장)
QA 기여: 가용성 보장, 확장성, 보안 강화 (QAS-04), 비용 효율성
