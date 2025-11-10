package com.smartfitness.access.ports;

import com.smartfitness.access.model.AccessGrantResult;
import com.smartfitness.access.model.AccessRequest;

/**
 * IAccessServiceApi: Entry point for access authorization requests.
 * Tactic: Application Façade.
 */
public interface IAccessServiceApi {
    /**
     * Request access grant based on provided face information.
     * @param request Face identification and vector data
     * @return AccessGrantResult outcome of the authorization
     */
    AccessGrantResult requestAccessGrant(AccessRequest request);
}
