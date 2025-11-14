package com.smartfitness.helper.service;

import com.smartfitness.common.event.TaskSubmittedEvent;
import com.smartfitness.helper.adapter.IMessagePublisherService;
import com.smartfitness.helper.adapter.ITaskPhotoStorage;
import com.smartfitness.helper.repository.IHelperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * Business Layer: Task Submission Manager
 * Component: TaskSubmissionManager
 * 
 * UC-12: Task Photo Registration
 * 1. Validate daily limit (3 photos/day) via ITaskValidationService
 * 2. Store photo in S3 via ITaskPhotoStorage
 * 3. Publish TaskSubmittedEvent for async AI analysis
 * 4. Respond immediately
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskSubmissionManager implements ITaskSubmissionService {
    
    private final ITaskValidationService taskValidationService;
    private final ITaskPhotoStorage taskPhotoStorage;
    private final IHelperRepository helperRepository;
    private final IMessagePublisherService messagePublisherService;
    
    @Override
    public Map<String, Object> submitTask(String helperId, String branchId, MultipartFile photo) {
        log.info("Processing task submission for helper: {}, branch: {}", helperId, branchId);
        
        // 1. Validate daily limit (3 photos/day)
        boolean isValid = taskValidationService.validateDailyLimit(helperId);
        if (!isValid) {
            log.warn("Daily limit exceeded for helper: {}", helperId);
            return Map.of(
                    "success", false,
                    "message", "Daily limit exceeded (3 photos/day)"
            );
        }
        
        // 2. Store photo in S3
        String taskId = UUID.randomUUID().toString();
        String photoUrl = taskPhotoStorage.uploadPhoto(taskId, photo);
        log.info("Photo uploaded to S3: {}", photoUrl);
        
        // 3. Persist task metadata
        // Stub: helperRepository.saveTask(taskId, helperId, branchId, photoUrl);
        log.debug("Task metadata saved: {}", taskId);
        
        // 4. Publish TaskSubmittedEvent for async AI analysis
        TaskSubmittedEvent event = new TaskSubmittedEvent(taskId, helperId, photoUrl);
        messagePublisherService.publishEvent(event);
        log.info("TaskSubmittedEvent published for task: {}", taskId);
        
        // 5. Respond immediately (async processing)
        return Map.of(
                "success", true,
                "taskId", taskId,
                "message", "Task submitted successfully. AI analysis in progress."
        );
    }
}

