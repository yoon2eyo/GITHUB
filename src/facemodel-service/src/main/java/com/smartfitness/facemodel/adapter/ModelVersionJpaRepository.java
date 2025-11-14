package com.smartfitness.facemodel.adapter;

import com.smartfitness.facemodel.domain.ModelVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * System Interface Layer: Model Version JPA Repository
 * Component: ModelVersionJpaRepository
 * DD-03: Database per Service (facemodel_db)
 * Stores model metadata for hot swap and rollback
 * Reference: 12_FaceModelServiceComponent.puml
 */
@Repository
public interface ModelVersionJpaRepository extends JpaRepository<ModelVersion, String>, IModelVersionRepository {
    
    @Override
    @Query("SELECT m FROM ModelVersion m WHERE m.status = 'ACTIVE' ORDER BY m.deployedAt DESC LIMIT 1")
    default ModelVersion findLatestActiveModel() {
        return findAll().stream()
                .filter(m -> m.getStatus() == ModelVersion.ModelStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
    
    @Override
    default ModelVersion findByVersionName(String versionName) {
        return findAll().stream()
                .filter(m -> versionName.equals(m.getVersionName()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    @Query("SELECT m FROM ModelVersion m WHERE m.status = 'INACTIVE' ORDER BY m.deactivatedAt DESC LIMIT 1")
    default ModelVersion findPreviousModel() {
        return findAll().stream()
                .filter(m -> m.getStatus() == ModelVersion.ModelStatus.INACTIVE)
                .findFirst()
                .orElse(null);
    }
}

