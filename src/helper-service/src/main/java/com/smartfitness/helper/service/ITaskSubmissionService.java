package com.smartfitness.helper.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Business Layer: Task Submission Service Interface
 * Reference: 04_HelperServiceComponent.puml (ITaskSubmissionService)
 */
public interface ITaskSubmissionService {
    Map<String, Object> submitTask(String helperId, String branchId, MultipartFile photo);
}

