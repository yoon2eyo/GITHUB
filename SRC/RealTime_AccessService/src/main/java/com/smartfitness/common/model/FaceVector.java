package com.smartfitness.common.model;

/**
 * FaceVector: Common representation of a face embedding/vector.
 * Used for both encrypted storage (Access Service) and in-memory pipeline processing (Face Model Service).
 */
public class FaceVector {
    private final byte[] data;

    public FaceVector(byte[] data) {
        this.data = data;
    }

    /** Raw vector bytes (used by Face Model pipeline). */
    public byte[] getData() {
        return data;
    }

    /**
     * Alias for Access Service contexts where the vector is stored encrypted.
     * Maintained for backward compatibility while eliminating duplicate model classes.
     */
    public byte[] getEncryptedVector() {
        return data;
    }
}
