package com.smartfitness.helper.service;

import com.smartfitness.helper.adapter.IMessageSubscriptionService;
import com.smartfitness.helper.repository.IHelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Business Layer: AI Task Analysis Consumer
 * Component: AITaskAnalysisConsumer
 * 
 * UC-13: AI Photo Analysis (Event-Driven)
 * Consumes: TaskSubmittedEvent
 * 
 * Flow:
 * 1. Subscribe to TaskSubmittedEvent
 * 2. Retrieve photo from storage
 * 3. Call ML Inference Engine via TaskAnalysisEngine
 * 4. Store analysis result in database
 * 
 * DD-02: Event-Based Architecture
 * - Async processing protects API responsiveness
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AITaskAnalysisConsumer {
    
    private final IMessageSubscriptionService messageSubscriptionService;
    private final ITaskAnalysisService taskAnalysisService;
    private final IHelperRepository helperRepository;
    
    /**
     * Subscribe to TaskSubmittedEvent
     * In production, use @RabbitListener
     */
    public void subscribeToTaskSubmittedEvent() {
        log.info("Subscribing to TaskSubmittedEvent");
        messageSubscriptionService.subscribe("TaskSubmittedEvent", this);
    }
    
    /**
     * Handle TaskSubmittedEvent
     * Called when a helper submits a task photo
     */
    public void handleTaskSubmittedEvent(String taskId, String helperId, String photoUrl) {
        log.info("Handling TaskSubmittedEvent: taskId={}, helperId={}", taskId, helperId);
        
        try {
            // 1. Analyze task via ML Inference Engine
            String analysisResult = taskAnalysisService.analyzeTask(taskId, photoUrl);
            
            // 2. Store analysis result
            // Stub: helperRepository.updateTaskAnalysis(taskId, analysisResult);
            log.info("Task analysis stored: {} - {}", taskId, analysisResult);
            
        } catch (Exception e) {
            log.error("Failed to analyze task: {}", taskId, e);
            // Stub: Store error status
        }
    }
}

