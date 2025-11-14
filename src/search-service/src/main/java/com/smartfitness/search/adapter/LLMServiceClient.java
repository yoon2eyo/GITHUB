package com.smartfitness.search.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * System Interface Layer: LLM Service Client
 * Component: LLMServiceClient
 * 
 * Implements: ILLMAnalysisServiceClient
 * Target: External LLM Service (HTTPS)
 * 
 * **Used ONLY in Cold Path** (UC-10, UC-18)
 * - NOT used in Hot Path search (UC-09)
 * - Ensures SLA guarantee for real-time search
 * 
 * DD-06, DD-09: Hot/Cold Path Separation (Approach 3)
 * - Cold Path can use external LLM
 * - Hot Path must be fast (NO external dependency)
 * 
 * Reference: 03_BranchContentServiceComponent.puml
 */
@Slf4j
@Component
public class LLMServiceClient implements ILLMAnalysisServiceClient {
    
    private static final String LLM_SERVICE_URL = "https://api.openai.com/v1/completions";
    
    @Override
    public Map<String, Object> extractKeywords(String content) {
        log.info("Calling external LLM service for keyword extraction: {} chars", content.length());
        
        // Stub: In production, use WebClient to call external LLM API
        // WebClient webClient = WebClient.create(LLM_SERVICE_URL);
        // Map<String, Object> request = Map.of(
        //     "model", "gpt-3.5-turbo",
        //     "prompt", "Extract keywords from: " + content,
        //     "max_tokens", 100
        // );
        // Map<String, Object> response = webClient.post()
        //     .bodyValue(request)
        //     .retrieve()
        //     .bodyToMono(Map.class)
        //     .block();
        
        // Stub: Simulate LLM processing delay
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Stub: Return mock keywords
        Map<String, Object> result = Map.of(
                "keywords", List.of("fitness", "gym", "equipment", "training"),
                "sentiment", "positive",
                "category", "fitness"
        );
        
        log.info("LLM keyword extraction completed: {} keywords", ((List<?>) result.get("keywords")).size());
        
        return result;
    }
}

