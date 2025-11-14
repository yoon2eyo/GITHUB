package com.smartfitness.helper.controller;

import com.smartfitness.helper.service.ITaskSubmissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: Task Controller
 * Component: TaskController
 * 
 * UC-12: Task Photo Registration
 * - Helper uploads task photo
 * - Validates daily limit (3 photos/day)
 * - Publishes TaskSubmittedEvent for async AI analysis
 * 
 * Reference: 04_HelperServiceComponent.puml (IHelperTaskApi)
 */
@Slf4j
@RestController
@RequestMapping("/helper/tasks")
@RequiredArgsConstructor
public class TaskController implements IHelperTaskApi {
    
    private final ITaskSubmissionService taskSubmissionService;
    
    /**
     * UC-12: Submit task photo
     * Validates daily limit and publishes event for async processing
     */
    @Override
    @PostMapping
    public ResponseEntity<Map<String, Object>> submitTask(
            @RequestParam String helperId,
            @RequestParam String branchId,
            @RequestParam("photo") MultipartFile photo) {
        
        log.info("Task submission request from helper: {}, branch: {}", helperId, branchId);
        
        Map<String, Object> result = taskSubmissionService.submitTask(helperId, branchId, photo);
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get task analysis status
     */
    @Override
    @GetMapping("/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        log.info("Get task status: {}", taskId);
        
        // Stub: Return task status
        return ResponseEntity.ok(Map.of(
                "taskId", taskId,
                "status", "ANALYZING",
                "message", "Task is being analyzed by AI"
        ));
    }
    
    /**
     * Get helper's task history
     */
    @Override
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getTaskHistory(@RequestParam String helperId) {
        log.info("Get task history for helper: {}", helperId);
        
        // Stub: Return task history
        return ResponseEntity.ok(Map.of(
                "helperId", helperId,
                "tasks", Map.of(),
                "totalTasks", 0
        ));
    }
}

