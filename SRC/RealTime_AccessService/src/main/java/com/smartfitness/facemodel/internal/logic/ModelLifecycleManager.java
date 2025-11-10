package com.smartfitness.facemodel.internal.logic;

import com.smartfitness.facemodel.ports.IModelManagementPort;
import com.smartfitness.mlo.model.LoadedModel;

/**
 * ModelLifecycleManager: Loads new models into memory and manages rollback.
 * Tactic: Hot Swap (QAS-06).
 */
public class ModelLifecycleManager implements IModelManagementPort {

    @Override
    public void loadNewModel(byte[] modelBinary) {
        LoadedModel newModel = LoadedModel.loadFromBinary(modelBinary);

        LoadedModel currentModel = VectorComparisonEngine.activeModel.get();
        if (currentModel != null) {
            VectorComparisonEngine.modelHistory.put(currentModel.getVersion(), currentModel);
        }

        VectorComparisonEngine.activeModel.set(newModel);
    }

    @Override
    public void rollbackToPreviousModel() {
        // Basic rollback: if history has any model, restore one arbitrarily
        for (LoadedModel previous : VectorComparisonEngine.modelHistory.values()) {
            VectorComparisonEngine.activeModel.set(previous);
            return;
        }
        throw new IllegalStateException("No previous model available for rollback.");
    }
}

