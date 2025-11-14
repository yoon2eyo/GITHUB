package com.smartfitness.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: Face Vector (512-dimensional embedding)
 * Used for: Face Recognition, Model Comparison
 * DD-05: IPC Data Transfer
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceVectorDto {
    private String userId;
    private float[] vector; // 512-dimensional float array
    private String modelVersion;
    private Long createdTimestamp;
    
    public int getDimension() {
        return vector != null ? vector.length : 0;
    }
    
    public boolean isValid() {
        return userId != null && vector != null && vector.length == 512;
    }
}

