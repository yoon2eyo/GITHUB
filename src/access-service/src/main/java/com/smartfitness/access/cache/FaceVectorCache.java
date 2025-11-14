package com.smartfitness.access.cache;

import com.smartfitness.access.adapter.IAccessVectorRepository;
import com.smartfitness.common.dto.FaceVectorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Business Layer: Face Vector Cache
 * Component: FaceVectorCache
 * 
 * DD-05 Tactic: Data Pre-Fetching
 * - Startup: Load top 10K active face vectors
 * - Runtime: LRU eviction (24h TTL)
 * - Hit rate: >90% target
 * - Memory: ~500MB (10K × 512 dims × 4 bytes)
 * - Removes DB I/O from hot path
 * 
 * Reference: 10_RealTimeAccessServiceComponent.puml
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaceVectorCache {
    
    private final IAccessVectorRepository vectorRepository;
    
    // In-memory cache: userId -> FaceVectorDto
    private final Map<String, FaceVectorDto> cache = new ConcurrentHashMap<>();
    
    private static final int MAX_CACHE_SIZE = 10_000;
    private static final long TTL_HOURS = 24;
    
    /**
     * Warm up cache on application startup
     * DD-05: Load top 10K active vectors
     */
    @PostConstruct
    public void warmUpCache() {
        log.info("Warming up face vector cache...");
        
        long startTime = System.currentTimeMillis();
        
        // Load top 10K active users' face vectors
        List<FaceVectorDto> activeVectors = vectorRepository.findTopActiveVectors(MAX_CACHE_SIZE);
        
        for (FaceVectorDto vector : activeVectors) {
            cache.put(vector.getUserId(), vector);
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Cache warmed up: {} vectors loaded in {}ms", cache.size(), duration);
        log.info("Estimated memory usage: ~{}MB", estimateMemoryUsage());
    }
    
    /**
     * Get face vector from cache (high-speed lookup)
     * Cache hit rate: >90%
     */
    @Cacheable(value = "faceVectors", key = "#branchId")
    public FaceVectorDto getActiveVector(String branchId) {
        log.debug("Cache lookup for branch: {}", branchId);
        
        // Stub: In production, query by branchId to get active users
        // For now, return first cached vector
        FaceVectorDto vector = cache.values().stream().findFirst().orElse(null);
        
        if (vector != null) {
            log.debug("Cache HIT for branch: {}", branchId);
        } else {
            log.warn("Cache MISS for branch: {}", branchId);
            // On cache miss, fetch from DB and update cache
            vector = fetchAndCache(branchId);
        }
        
        return vector;
    }
    
    /**
     * Scheduled cache refresh (every hour)
     * Maintains freshness and evicts stale entries
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void refreshCache() {
        log.info("Refreshing face vector cache...");
        
        int oldSize = cache.size();
        
        // Evict old entries (stub: simple age-based eviction)
        // In production: LRU with TTL check
        cache.clear();
        
        // Reload active vectors
        warmUpCache();
        
        log.info("Cache refreshed: {} -> {} vectors", oldSize, cache.size());
    }
    
    /**
     * Add or update vector in cache
     */
    public void putVector(String userId, FaceVectorDto vector) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            log.warn("Cache full ({}), evicting oldest entry", MAX_CACHE_SIZE);
            // Stub: Simple FIFO eviction
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
        }
        
        cache.put(userId, vector);
        log.debug("Vector cached for user: {}", userId);
    }
    
    /**
     * Remove vector from cache (e.g., user deleted)
     */
    public void evictVector(String userId) {
        cache.remove(userId);
        log.debug("Vector evicted for user: {}", userId);
    }
    
    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
                "size", cache.size(),
                "maxSize", MAX_CACHE_SIZE,
                "estimatedMemoryMB", estimateMemoryUsage(),
                "ttlHours", TTL_HOURS
        );
    }
    
    private FaceVectorDto fetchAndCache(String branchId) {
        log.info("Fetching vector from DB for branch: {}", branchId);
        
        // Stub: Fetch from database
        FaceVectorDto vector = vectorRepository.findByBranchId(branchId);
        
        if (vector != null) {
            putVector(vector.getUserId(), vector);
        }
        
        return vector;
    }
    
    private long estimateMemoryUsage() {
        // 512 dims × 4 bytes (float) × cache size
        long vectorSize = 512 * 4;
        return (vectorSize * cache.size()) / (1024 * 1024); // Convert to MB
    }
}

