package com.smartfitness.mlo.external.helper;

import com.smartfitness.domain.model.HelperTask;
import com.smartfitness.mlo.ports.IHelperRepository;
import java.util.List;
import java.util.Objects;

/**
 * Bridges the Helper read-only export API to the MLOps port.
 */
public class HelperRepositoryAdapter implements IHelperRepository {
    private final RemoteHelperDataClient remoteClient;

    public HelperRepositoryAdapter(RemoteHelperDataClient remoteClient) {
        this.remoteClient = Objects.requireNonNull(remoteClient, "remoteClient");
    }

    @Override
    public List<HelperTask> findConfirmedTasksForRetraining() {
        return remoteClient.fetchConfirmedTasksForRetraining();
    }
}
