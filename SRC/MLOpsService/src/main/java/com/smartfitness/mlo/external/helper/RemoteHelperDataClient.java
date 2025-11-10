package com.smartfitness.mlo.external.helper;

import com.smartfitness.domain.model.HelperTask;
import java.util.List;

/**
 * Read-only client facade for Helper Service data exports.
 */
public interface RemoteHelperDataClient {
    List<HelperTask> fetchConfirmedTasksForRetraining();
}
