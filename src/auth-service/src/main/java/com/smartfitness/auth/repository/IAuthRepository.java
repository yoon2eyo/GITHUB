package com.smartfitness.auth.repository;

import com.smartfitness.auth.domain.User;

/**
 * System Interface Layer: Auth Repository Interface
 * Reference: 02_AuthenticationServiceComponent.puml
 */
public interface IAuthRepository {
    User findByEmail(String email);
    User findById(String userId);
    void save(User user);
    void delete(User user);
}

