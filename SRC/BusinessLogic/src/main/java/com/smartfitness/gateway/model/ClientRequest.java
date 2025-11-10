package com.smartfitness.gateway.model;

/**
 * ClientRequest: Minimal HTTP-like request representation for the gateway.
 */
public class ClientRequest {
    private final String path;
    private final String method;
    private final String authToken;
    private final String body;
    private final String signature;
    private final String clientIp;

    public ClientRequest(String path, String method, String authToken, String body) {
        this(path, method, authToken, body, null, null);
    }

    public ClientRequest(String path,
                         String method,
                         String authToken,
                         String body,
                         String signature,
                         String clientIp) {
        this.path = path;
        this.method = method;
        this.authToken = authToken;
        this.body = body;
        this.signature = signature;
        this.clientIp = clientIp;
    }

    public String getPath() { return path; }
    public String getMethod() { return method; }
    public String getAuthToken() { return authToken; }
    public String getBody() { return body; }
    public String getSignature() { return signature; }
    public String getClientIp() { return clientIp; }
}
