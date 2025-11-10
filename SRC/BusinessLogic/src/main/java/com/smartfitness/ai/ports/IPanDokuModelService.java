package com.smartfitness.ai.ports;

/**
 * IPanDokuModelService: External AI model service contract for single-image inference.
 */
public interface IPanDokuModelService {
    String requestPanDoku(String imageUrl);
}

