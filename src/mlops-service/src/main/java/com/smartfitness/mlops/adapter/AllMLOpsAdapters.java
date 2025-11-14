package com.smartfitness.mlops.adapter;

import com.smartfitness.common.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MLOps Service - System Interface Layer Adapters
 * Reference: 11_MLOpsServiceComponent.puml
 */

// ========== INTERFACES ==========

interface IMLInferenceEngine {
    void deployModel(String modelId);
    void trainModel(String trainingId);
}

interface IMessagePublisherService {
    void publishEvent(DomainEvent event);
}

interface IMessageSubscriptionService {
    void subscribe(String eventType, Object consumer);
}

interface IFaceModelClient {
    void notifyModelUpdate(String modelId);
}

interface IAuthRepository {
    // READ-ONLY access for training data collection
    void findAllFaceVectors();
}

interface IHelperRepository {
    // READ-ONLY access for training data collection
    void findAllTaskPhotos();
}

// ========== IMPLEMENTATIONS ==========

@Slf4j
@Component
class MLInferenceEngineAdapter implements IMLInferenceEngine {
    
    @Override
    public void deployModel(String modelId) {
        log.info("Deploying model to ML Inference Engine: {}", modelId);
    }
    
    @Override
    public void trainModel(String trainingId) {
        log.info("Training model via ML Inference Engine: {}", trainingId);
    }
}

@Slf4j
@Component
class RabbitMQAdapter implements IMessagePublisherService, IMessageSubscriptionService {
    
    @Override
    public void publishEvent(DomainEvent event) {
        log.info("Publishing event to RabbitMQ: {} ({})", event.getEventType(), event.getEventId());
    }
    
    @Override
    public void subscribe(String eventType, Object consumer) {
        log.info("Subscribing to event type: {}", eventType);
    }
}

@Slf4j
@Component
class FaceModelClientAdapter implements IFaceModelClient {
    
    @Override
    public void notifyModelUpdate(String modelId) {
        log.info("Notifying FaceModel Service of model update (Hot Swap): {}", modelId);
        // gRPC call to FaceModel Service
    }
}

@Slf4j
@Component
class AuthRepositoryAdapter implements IAuthRepository {
    
    @Override
    public void findAllFaceVectors() {
        log.info("READ-ONLY: Collecting face vectors from Auth DB for training");
        // DD-03 Exception: JDBC READ-ONLY access
    }
}

@Slf4j
@Component
class HelperRepositoryAdapter implements IHelperRepository {
    
    @Override
    public void findAllTaskPhotos() {
        log.info("READ-ONLY: Collecting task photos from Helper DB for training");
        // DD-03 Exception: JDBC READ-ONLY access
    }
}

