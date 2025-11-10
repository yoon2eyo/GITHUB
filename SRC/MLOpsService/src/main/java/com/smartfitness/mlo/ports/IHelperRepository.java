package com.smartfitness.mlo.ports;

import java.util.List;

import com.smartfitness.domain.model.HelperTask;

/**
 * Required Port: Accesses data owned by the Helper Service (for collecting ground truth).
 */
public interface IHelperRepository {
    /**
     * Collects tasks recently confirmed by the Branch Owner (ground truth for retraining).
     */
    List<HelperTask> findConfirmedTasksForRetraining();
}