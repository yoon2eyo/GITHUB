package com.smartfitness.helper.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * System Interface Layer: S3 Photo Storage
 * Component: S3PhotoStorage
 * 
 * Implements: ITaskPhotoStorage
 * Storage: AWS S3
 * 
 * UC-12: Upload task photos to S3
 * UC-13: Download task photos from S3 for AI analysis
 * 
 * Reference: 04_HelperServiceComponent.puml
 */
@Slf4j
@Component
public class S3PhotoStorage implements ITaskPhotoStorage {
    
    private static final String S3_BUCKET = "smart-fitness-tasks";
    private static final String S3_BASE_URL = "https://s3.amazonaws.com/" + S3_BUCKET;
    
    @Override
    public String uploadPhoto(String taskId, MultipartFile photo) {
        log.info("Uploading photo to S3: taskId={}, size={} bytes", taskId, photo.getSize());
        
        // Stub: In production, use AWS SDK S3Client
        // String key = "tasks/" + taskId + "/" + photo.getOriginalFilename();
        // PutObjectRequest request = PutObjectRequest.builder()
        //     .bucket(S3_BUCKET)
        //     .key(key)
        //     .build();
        // s3Client.putObject(request, RequestBody.fromBytes(photo.getBytes()));
        
        String photoUrl = S3_BASE_URL + "/tasks/" + taskId + "/" + photo.getOriginalFilename();
        log.info("Photo uploaded successfully: {}", photoUrl);
        
        return photoUrl;
    }
    
    @Override
    public byte[] downloadPhoto(String photoUrl) {
        log.info("Downloading photo from S3: {}", photoUrl);
        
        // Stub: In production, use AWS SDK S3Client
        // String key = extractKeyFromUrl(photoUrl);
        // GetObjectRequest request = GetObjectRequest.builder()
        //     .bucket(S3_BUCKET)
        //     .key(key)
        //     .build();
        // ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);
        // return response.asByteArray();
        
        // Stub: Return dummy bytes
        byte[] dummyPhoto = new byte[1024];
        log.debug("Photo downloaded: {} bytes", dummyPhoto.length);
        
        return dummyPhoto;
    }
}

