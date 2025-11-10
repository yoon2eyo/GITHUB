package com.smartfitness.system.discovery;

import com.smartfitness.system.exception.ServiceUnavailableException;
import java.util.List;

/**
 * ServiceDiscovery: Resolves service endpoints and balances across instances.
 */
public interface ServiceDiscovery {
    String resolveAndBalance(String servicePath) throws ServiceUnavailableException;
    List<String> getServiceInstances(String servicePath);
}

