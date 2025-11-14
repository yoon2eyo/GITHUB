package com.smartfitness.helper.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Interface Layer: Helper Task API Interface
 * Reference: 04_HelperServiceComponent.puml (IHelperTaskApi)
 */
public interface IHelperTaskApi {
    ResponseEntity<Map<String, Object>> submitTask(String helperId, String branchId, MultipartFile photo);
    ResponseEntity<Map<String, Object>> getTaskStatus(String taskId);
    ResponseEntity<Map<String, Object>> getTaskHistory(String helperId);
}

