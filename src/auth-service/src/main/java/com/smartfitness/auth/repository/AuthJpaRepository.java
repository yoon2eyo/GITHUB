package com.smartfitness.auth.repository;

import com.smartfitness.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * System Interface Layer: Auth JPA Repository
 * Component: AuthJpaRepository
 * Provides database access for User entities
 * DD-03: Database per Service
 * Reference: 02_AuthenticationServiceComponent.puml
 */
@Repository
public interface AuthJpaRepository extends JpaRepository<User, String>, IAuthRepository {
    
    @Override
    default User findByEmail(String email) {
        return findAll().stream()
                .filter(user -> email.equals(user.getEmail()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    default User findById(String userId) {
        return findById(userId).orElse(null);
    }
}

