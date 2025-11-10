package com.smartfitness.persistence;

import com.smartfitness.auth.ports.IAuthRepository;
import com.smartfitness.domain.model.UserAccount;

import java.util.Optional;
import javax.sql.DataSource;

/**
 * AuthRepositoryImpl: DAL for DB_AUTH as required by DD-03.
 */
public class AuthRepositoryImpl implements IAuthRepository {
    private final DataSource dataSource;

    public AuthRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        // TODO: SELECT ... FROM DB_AUTH.Users WHERE username = ?
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void saveUser(UserAccount user) {
        // TODO: INSERT into DB_AUTH.Users
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String loadPasswordHash(String userId) {
        // TODO: SELECT password_hash FROM DB_AUTH.Users WHERE id = ?
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
