package com.smartfitness.helper.ports;

/**
 * IAIPanDokuService: Requests asynchronous AI review (LLM pipeline).
 */
public interface IAIPanDokuService {
    /**
     * Request initial AI review for the submitted task (UC-13).
     */
    void requestInitialPanDoku(Long taskId, String imageUrl);
}
