package com.smartfitness.gateway.model;

/**
 * ServiceResponse: Minimal response wrapper used by the gateway.
 */
public class ServiceResponse {
    private final int status;
    private final String body;

    private ServiceResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public static ServiceResponse OK(String body) { return new ServiceResponse(200, body); }
    public static ServiceResponse FORBIDDEN() { return new ServiceResponse(403, "Forbidden"); }

    // Extensions per BusinessLogic.md
    public static ServiceResponse FORBIDDEN(String message) { return new ServiceResponse(403, message); }
    public static ServiceResponse NOT_FOUND(String message) { return new ServiceResponse(404, message); }
    public static ServiceResponse SERVICE_UNAVAILABLE() { return new ServiceResponse(503, "Service Unavailable"); }

    public int getStatus() { return status; }
    public String getBody() { return body; }
}
