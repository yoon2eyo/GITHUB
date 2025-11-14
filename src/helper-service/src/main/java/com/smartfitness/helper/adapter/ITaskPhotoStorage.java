package com.smartfitness.helper.adapter;

import org.springframework.web.multipart.MultipartFile;

/**
 * System Interface Layer: Task Photo Storage Interface
 * Reference: 04_HelperServiceComponent.puml (ITaskPhotoStorage)
 */
public interface ITaskPhotoStorage {
    String uploadPhoto(String taskId, MultipartFile photo);
    byte[] downloadPhoto(String photoUrl);
}

