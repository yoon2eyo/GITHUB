package com.smartfitness.gateway.security;

import com.smartfitness.gateway.model.ClientRequest;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Verifies request integrity using an HmacSHA256 signature.
 */
public class RequestSignatureVerifier {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private final byte[] sharedSecret;

    public RequestSignatureVerifier(String sharedSecret) {
        this.sharedSecret = sharedSecret.getBytes(StandardCharsets.UTF_8);
    }

    public boolean verify(ClientRequest request) {
        if (request.getSignature() == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(sharedSecret, HMAC_ALGORITHM));
            String payload = (request.getPath() == null ? "" : request.getPath())
                + "|" + (request.getMethod() == null ? "" : request.getMethod())
                + "|" + (request.getBody() == null ? "" : request.getBody());
            byte[] computed = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = Base64.getEncoder().encodeToString(computed);
            return expectedSignature.equals(request.getSignature());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to verify request signature", e);
        }
    }
}
