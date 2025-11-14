package com.smartfitness.facemodel.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Domain Entity: Model Version
 * Tracks ML model versions for hot swap and rollback
 * QAS-06: Zero-downtime deployment
 */
@Entity
@Table(name = "model_versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String versionId;
    
    @Column(nullable = false, unique = true)
    private String versionName; // e.g., "v1.0.0", "v1.1.0"
    
    @Column(nullable = false)
    private String modelPath; // File system or S3 path
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModelStatus status; // ACTIVE, INACTIVE, TESTING
    
    @Column
    private Double accuracy; // Model accuracy metric
    
    @Column
    private Long inferenceTimeMs; // Average inference time
    
    @Column(nullable = false)
    private Instant deployedAt;
    
    @Column
    private Instant deactivatedAt;
    
    @Column
    private String deployedBy; // MLOps service or admin
    
    public enum ModelStatus {
        ACTIVE,
        INACTIVE,
        TESTING,
        ROLLBACK
    }
}

