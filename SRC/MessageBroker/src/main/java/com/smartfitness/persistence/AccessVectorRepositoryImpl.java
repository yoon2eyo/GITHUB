package com.smartfitness.persistence;

import com.smartfitness.access.ports.IAccessVectorRepository;
import com.smartfitness.common.model.FaceVector;

import java.util.Optional;
import javax.sql.DataSource;

public class AccessVectorRepositoryImpl implements IAccessVectorRepository {
    private final DataSource dataSource;

    public AccessVectorRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<FaceVector> findVectorById(String faceId) {
        // TODO: Query DS-02 for encrypted face vector
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
