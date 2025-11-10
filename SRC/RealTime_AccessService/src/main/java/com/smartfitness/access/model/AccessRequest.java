package com.smartfitness.access.model;

/**
 * AccessRequest: Face identification and vector payload from device/API.
 */
public class AccessRequest {
    private final String faceId;
    private final byte[] vectorData;
    private final String equipmentId;

    public AccessRequest(String faceId, byte[] vectorData, String equipmentId) {
        this.faceId = faceId;
        this.vectorData = vectorData;
        this.equipmentId = equipmentId;
    }

    public String getFaceId() { return faceId; }
    public byte[] getVectorData() { return vectorData; }
    public String getEquipmentId() { return equipmentId; }
}
