package com.smartfitness.access.api;

import com.smartfitness.access.internal.logic.AccessAuthorizationManager;
import com.smartfitness.access.model.AccessGrantResult;
import com.smartfitness.access.model.AccessRequest;
import com.smartfitness.access.ports.IAccessServiceApi;

/**
 * AccessServiceApiImpl: Concrete façade delegating to AccessAuthorizationManager.
 */
public class AccessServiceApiImpl implements IAccessServiceApi {
    private final AccessAuthorizationManager authorizationManager;

    public AccessServiceApiImpl(AccessAuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Override
    public AccessGrantResult requestAccessGrant(AccessRequest request) {
        return authorizationManager.requestAccessGrant(request);
    }
}
