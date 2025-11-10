package com.smartfitness.gateway.ports;

import com.smartfitness.gateway.model.ClientRequest;
import com.smartfitness.gateway.model.ServiceResponse;

/**
 * IApiGatewayEntry: Front Controller entry point for all client requests.
 */
public interface IApiGatewayEntry {
    /**
     * Route client request to the appropriate backend service.
     * @param request client HTTP-like request
     * @return response from downstream service
     */
    ServiceResponse routeRequest(ClientRequest request);
}

