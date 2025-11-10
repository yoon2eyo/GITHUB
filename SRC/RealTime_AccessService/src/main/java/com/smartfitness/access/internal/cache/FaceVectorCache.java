package com.smartfitness.access.internal.cache;

import com.smartfitness.access.ports.IAccessVectorRepository;
import com.smartfitness.common.model.FaceVector;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FaceVectorCache: Provides warm cache + asynchronous refresh for frequently accessed face vectors
 * to avoid hitting DS-02 for every authorization request (DD-05 Data Pre-Fetching).
 */
public class FaceVectorCache {
    private final IAccessVectorRepository repository;
    private final ConcurrentHashMap<String, byte[]> cache = new ConcurrentHashMap<>();
    private final ExecutorService refreshPool = Executors.newSingleThreadExecutor();
    private final int maxEntries;

    public FaceVectorCache(IAccessVectorRepository repository, int maxEntries) {
        this.repository = repository;
        this.maxEntries = Math.max(1, maxEntries);
    }

    /**
     * Preload known hot face IDs (e.g., staff members, frequent visitors) asynchronously.
     */
    public void warmUp(List<String> hotFaceIds) {
        if (hotFaceIds == null) {
            return;
        }
        for (String faceId : hotFaceIds) {
            refreshPool.submit(() -> repository.findVectorById(faceId).ifPresent(vector -> put(faceId, vector.getEncryptedVector())));
        }
    }

    /**
     * Retrieve a cached vector or load it from DS-02 on demand. Loaded entries are cached and refreshed in background.
     */
    public Optional<byte[]> getOrLoad(String faceId) {
        byte[] cached = cache.get(faceId);
        if (cached != null) {
            scheduleRefresh(faceId);
            return Optional.of(cached);
        }
        Optional<FaceVector> loaded = repository.findVectorById(faceId);
        loaded.ifPresent(vector -> put(faceId, vector.getEncryptedVector()));
        return loaded.map(FaceVector::getEncryptedVector);
    }

    private synchronized void put(String faceId, byte[] data) {
        if (cache.size() >= maxEntries) {
            var iterator = cache.keySet().iterator();
            if (iterator.hasNext()) {
                cache.remove(iterator.next());
            }
        }
        cache.put(faceId, data);
    }

    private void scheduleRefresh(String faceId) {
        CompletableFuture.runAsync(() ->
            repository.findVectorById(faceId)
                .ifPresent(vector -> cache.put(faceId, vector.getEncryptedVector())),
            refreshPool
        );
    }

    public void shutdown() {
        refreshPool.shutdown();
    }
}
