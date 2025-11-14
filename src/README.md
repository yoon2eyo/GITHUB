# Smart Fitness System - Microservice Architecture

## 📋 Overview
스마트 피트니스 관리 시스템 - Hybrid MSA (4-Layer Distributed Service Architecture)

## 🏗️ Architecture

### Service List (11 Services)
1. **api-gateway-service** - API Gateway (Request Router)
2. **auth-service** - Authentication & Authorization
3. **access-service** - Real-Time Access Control (Face Recognition)
4. **facemodel-service** - Face Vector Comparison Engine
5. **search-service** - Branch Content Search & Review
6. **helper-service** - Helper Task Management
7. **branchowner-service** - Branch Owner Management
8. **monitoring-service** - Equipment Monitoring
9. **notification-service** - Push Notification Dispatcher
10. **mlops-service** - ML Training & Deployment
11. **common** - Shared Domain Events, DTOs, Utilities

## 🛠️ Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 3.2.x
- **Build Tool**: Gradle (Multi-module)
- **Message Broker**: RabbitMQ
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL (per service), Redis (cache)
- **Search Engine**: ElasticSearch
- **Storage**: AWS S3
- **Containerization**: Docker, Kubernetes

## 📁 Project Structure
```
src/
├── common/                      # Common module
├── api-gateway-service/         # API Gateway
├── auth-service/                # Authentication
├── access-service/              # Real-Time Access
├── facemodel-service/           # Face Model
├── search-service/              # Search & Review
├── helper-service/              # Helper Management
├── branchowner-service/         # Branch Owner
├── monitoring-service/          # Equipment Monitoring
├── notification-service/        # Notification
├── mlops-service/               # MLOps
├── build.gradle                 # Root build config
└── settings.gradle              # Multi-module settings
```

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Docker & Docker Compose
- Gradle 8.x

### Build
```bash
cd src
./gradlew clean build
```

### Run Services
```bash
# Start infrastructure (RabbitMQ, PostgreSQL, Redis, etc.)
docker-compose up -d

# Run individual service
./gradlew :api-gateway-service:bootRun
```

## 📖 Design Decisions
컴포넌트 다이어그램 및 설계 결정사항은 `/ComponentDiagram` 및 `/DD` 폴더를 참고하세요.

### Key Design Decisions (DD)
- DD-01: Hybrid MSA (4-Layer)
- DD-02: Async Event-Driven (RabbitMQ)
- DD-03: Database per Service
- DD-04: Heartbeat & Ping/echo Fault Detection
- DD-05: IPC Optimization for Face Recognition
- DD-06: Hot/Cold Path Separation for Search
- DD-07: Scheduling Policy for Matching
- DD-08: Multi-Layer Security (SSL/TLS, Token, Private Network)
- DD-09: Real-time Search (No LLM in Hot Path)

## 🎯 Quality Attributes
- **QAS-01**: Equipment fault alert within 15 seconds
- **QAS-02**: Face recognition access within 3 seconds (95%)
- **QAS-03**: Branch search response within 3 seconds (95%)
- **QAS-06**: Zero-downtime model deployment (<1ms hot swap)

## 📝 Notes
- This is a **stub implementation** based on component diagrams
- Each service follows 3-layer architecture: Interface, Business, System Interface
- All interfaces and components are implemented as stubs for scaffolding

---
**Generated**: 2025-11-11  
**Version**: 1.0.0-SNAPSHOT

