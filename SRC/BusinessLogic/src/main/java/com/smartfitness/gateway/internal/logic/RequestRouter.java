package com.smartfitness.gateway.internal.logic;

import com.smartfitness.gateway.model.ClientRequest;
import com.smartfitness.gateway.model.ServiceResponse;
import com.smartfitness.gateway.ports.IApiGatewayEntry;
import com.smartfitness.gateway.ports.IAuthenticationClient;
import com.smartfitness.gateway.security.NetworkZonePolicy;
import com.smartfitness.gateway.security.RequestSignatureVerifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RequestRouter: API Gateway core router. Validates and forwards requests.
 * Pattern: Front Controller, Broker.
 * DD-08: Verifies message integrity (HMAC) and enforces Limit Access between public/private zones.
 */
public class RequestRouter implements IApiGatewayEntry {
    private final IAuthenticationClient authClient;
    private final InternalClientManager internalClientManager;
    private final RequestSignatureVerifier signatureVerifier;
    private final NetworkZonePolicy networkZonePolicy;

    private static final Map<String, String> SERVICE_ROUTES = new ConcurrentHashMap<>();
    static {
        SERVICE_ROUTES.put("/auth", "AuthService");
        SERVICE_ROUTES.put("/access", "AccessService");
        SERVICE_ROUTES.put("/search", "SearchService");
        SERVICE_ROUTES.put("/helper", "HelperService");
    }

    public RequestRouter(IAuthenticationClient authClient,
                         InternalClientManager internalClientManager,
                         RequestSignatureVerifier signatureVerifier,
                         NetworkZonePolicy networkZonePolicy) {
        this.authClient = authClient;
        this.internalClientManager = internalClientManager;
        this.signatureVerifier = signatureVerifier;
        this.networkZonePolicy = networkZonePolicy;
    }

    @Override
    public ServiceResponse routeRequest(ClientRequest request) {
        if (!signatureVerifier.verify(request)) {
            return ServiceResponse.FORBIDDEN("Invalid or missing signature.");
        }

        if (!processSecurityCheck(request)) {
            return ServiceResponse.FORBIDDEN("Invalid or missing token.");
        }

        String targetServicePath = resolveTargetServicePath(request.getPath());
        if (targetServicePath == null) {
            return ServiceResponse.NOT_FOUND("Endpoint not found.");
        }

        if (!networkZonePolicy.isRouteAllowed(request, targetServicePath)) {
            return ServiceResponse.FORBIDDEN("Route blocked by network policy.");
        }

        return internalClientManager.forwardRequest(targetServicePath, request);
    }

    private boolean processSecurityCheck(ClientRequest request) {
        String path = request.getPath();
        if (isPublicEndpoint(path)) {
            return true;
        }
        return authClient.validateToken(request.getAuthToken());
    }

    private String resolveTargetServicePath(String fullPath) {
        if (fullPath == null || fullPath.isBlank()) return null;
        String[] parts = fullPath.split("/");
        if (parts.length < 2) return null;
        String pathPrefix = "/" + parts[1];
        return SERVICE_ROUTES.get(pathPrefix);
    }

    private boolean isPublicEndpoint(String path) {
        if (path == null) return false;
        return path.startsWith("/auth/login") || path.startsWith("/auth/register");
    }
}
