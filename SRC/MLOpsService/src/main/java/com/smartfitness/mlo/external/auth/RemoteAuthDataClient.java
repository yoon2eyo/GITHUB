package com.smartfitness.mlo.external.auth;

import com.smartfitness.domain.model.UserAccount;
import java.util.Date;
import java.util.List;

/**
 * Abstraction over the Auth Service export API used strictly for read-only data pulls.
 */
public interface RemoteAuthDataClient {
    List<UserAccount> fetchRecentRegistrations(Date sinceDate);
}
