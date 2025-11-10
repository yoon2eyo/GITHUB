package com.smartfitness.auth.ports;

import java.util.Optional;

import com.smartfitness.domain.model.UserAccount;

/**
 * IAuthRepository: Access to DB_AUTH (Database per Service).
 */
public interface IAuthRepository {
    Optional<UserAccount> findByUsername(String username);
    void saveUser(UserAccount user);
    String loadPasswordHash(String userId);
}

