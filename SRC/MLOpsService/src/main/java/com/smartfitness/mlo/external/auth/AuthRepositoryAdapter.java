package com.smartfitness.mlo.external.auth;

import com.smartfitness.domain.model.UserAccount;
import com.smartfitness.mlo.ports.IAuthRepository;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Bridges the MLOps read-only port to the Auth Service export API.
 */
public class AuthRepositoryAdapter implements IAuthRepository {
    private final RemoteAuthDataClient remoteClient;

    public AuthRepositoryAdapter(RemoteAuthDataClient remoteClient) {
        this.remoteClient = Objects.requireNonNull(remoteClient, "remoteClient");
    }

    @Override
    public List<UserAccount> findRecentlyRegisteredUsers(Date sinceDate) {
        return remoteClient.fetchRecentRegistrations(sinceDate);
    }
}
