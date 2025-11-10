package com.smartfitness.gateway.security;

import com.smartfitness.gateway.model.ClientRequest;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Enforces Limit Access by ensuring only trusted zones can reach private services.
 */
public class NetworkZonePolicy {
    public enum Zone { PUBLIC, PRIVATE }

    private final Map<String, Zone> serviceZones;
    private final Set<String> privateIpPrefixes;

    public NetworkZonePolicy(Map<String, Zone> serviceZones, Set<String> privateIpPrefixes) {
        this.serviceZones = Objects.requireNonNull(serviceZones, "serviceZones");
        this.privateIpPrefixes = Objects.requireNonNull(privateIpPrefixes, "privateIpPrefixes");
    }

    public boolean isRouteAllowed(ClientRequest request, String targetService) {
        Zone zone = serviceZones.getOrDefault(targetService, Zone.PUBLIC);
        if (zone == Zone.PUBLIC) {
            return true;
        }
        return isPrivateSource(request.getClientIp());
    }

    private boolean isPrivateSource(String clientIp) {
        if (clientIp == null) {
            return false;
        }
        return privateIpPrefixes.stream().anyMatch(clientIp::startsWith);
    }
}
