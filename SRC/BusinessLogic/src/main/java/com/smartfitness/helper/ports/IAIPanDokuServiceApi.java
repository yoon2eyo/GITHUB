package com.smartfitness.helper.ports;

/**
 * AI PanDoku 서비스와의 통신을 위한 인터페이스입니다.
 */
public interface IAIPanDokuServiceApi {
    /**
     * AI PanDoku 서비스에 초기 검토를 요청합니다.
     * 
     * @param taskId 검토할 작업의 ID
     * @param imageUrl 검토할 이미지의 URL
     */
    void requestInitialPanDoku(Long taskId, String imageUrl);
}