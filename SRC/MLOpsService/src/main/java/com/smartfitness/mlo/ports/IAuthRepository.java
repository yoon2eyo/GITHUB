package com.smartfitness.mlo.ports;

import java.util.List;

import com.smartfitness.domain.model.UserAccount;

import java.util.Date;

/**
 * Required Port: Accesses data owned by the Auth Service (for collecting face vectors).
 */
public interface IAuthRepository {
    /**
     * Only includes methods necessary for MLOps to collect data (e.g., successful registrations).
     */
    List<UserAccount> findRecentlyRegisteredUsers(Date sinceDate);
}