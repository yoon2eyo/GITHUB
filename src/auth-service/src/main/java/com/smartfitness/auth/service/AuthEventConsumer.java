package com.smartfitness.auth.service;

import com.smartfitness.auth.adapter.IMessageSubscriptionService;
import com.smartfitness.auth.repository.IAuthRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: Auth Event Consumer
 * Component: AuthEventConsumer
 * Subscribes to auth-related events from Message Broker
 * DD-02: Event-Based Architecture
 * Reference: 02_AuthenticationServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventConsumer {
    
    private final IAuthRepository authRepository;
    private final IMessageSubscriptionService messageSubscriptionService;
    
    /**
     * Subscribe to FaceVectorSyncEvent (when face vectors are updated)
     */
    public void subscribeFaceVectorSync() {
        log.info("Subscribing to FaceVectorSyncEvent");
        
        // Stub: In production, use @RabbitListener
        // @RabbitListener(queues = "auth.face-vector-sync")
        // public void handleFaceVectorSync(FaceVectorSyncEvent event) { ... }
        
        messageSubscriptionService.subscribe("FaceVectorSyncEvent", this);
    }
    
    /**
     * Handle face vector sync event
     */
    public void handleFaceVectorSync(String userId, String faceVectorData) {
        log.info("Handling FaceVectorSyncEvent for user: {}", userId);
        
        // Stub: Update user's face vector in database
        // User user = authRepository.findById(userId);
        // user.setFaceVectorData(faceVectorData);
        // authRepository.save(user);
        
        log.debug("Face vector synced for user: {}", userId);
    }
}

