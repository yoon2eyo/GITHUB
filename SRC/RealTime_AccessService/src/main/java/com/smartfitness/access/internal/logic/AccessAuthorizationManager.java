package com.smartfitness.access.internal.logic;

import com.smartfitness.access.internal.cache.FaceVectorCache;
import com.smartfitness.access.model.AccessGrantResult;
import com.smartfitness.access.model.AccessRequest;
import com.smartfitness.access.ports.IAccessServiceApi;
import com.smartfitness.access.ports.IAccessVectorRepository;
import com.smartfitness.common.model.FaceVector;
import com.smartfitness.event.AccessAttemptEvent;
import com.smartfitness.facemodel.ports.IFaceModelService;
import com.smartfitness.messaging.IMessagePublisherService;
import java.util.List;
import java.util.Optional;

/**
 * AccessAuthorizationManager: Core business logic for access authorization.
 * Tactic: Separate Entities.
 */
public class AccessAuthorizationManager implements IAccessServiceApi {
    private final IFaceModelService modelClient;
    private final GateController gateController;
    private final IMessagePublisherService messagePublisher;
    private final FaceVectorCache vectorCache;

    public AccessAuthorizationManager(IAccessVectorRepository vectorRepository,
                                      IFaceModelService modelClient,
                                      GateController gateController,
                                      IMessagePublisherService messagePublisher) {
        this(vectorRepository, modelClient, gateController, messagePublisher,
            new FaceVectorCache(vectorRepository, 1024), List.of());
    }

    public AccessAuthorizationManager(IAccessVectorRepository vectorRepository,
                                      IFaceModelService modelClient,
                                      GateController gateController,
                                      IMessagePublisherService messagePublisher,
                                      FaceVectorCache faceVectorCache,
                                      List<String> hotFaceIds) {
        this.modelClient = modelClient;
        this.gateController = gateController;
        this.messagePublisher = messagePublisher;
        this.vectorCache = faceVectorCache;
        this.vectorCache.warmUp(hotFaceIds);
    }

    @Override
    public AccessGrantResult requestAccessGrant(AccessRequest request) {
        Optional<byte[]> cachedVectorOpt = vectorCache.getOrLoad(request.getFaceId());
        AccessGrantResult result;

        if (cachedVectorOpt.isEmpty()) {
            result = AccessGrantResult.DENIED("Unregistered Face ID.");
        } else {
            double similarityScore = modelClient.calculateSimilarityScore(
                new FaceVector(request.getVectorData()),
                new FaceVector(cachedVectorOpt.get())
            );

            if (similarityScore >= 0.95d) {
                result = AccessGrantResult.GRANTED("Access Approved.");
            } else {
                result = AccessGrantResult.DENIED("Low Similarity Score.");
            }
        }

        gateController.controlGate(result, request.getEquipmentId());
        messagePublisher.publishEvent("access", new AccessAttemptEvent(request.getFaceId(), result.isGranted()));
        return result;
    }
}
