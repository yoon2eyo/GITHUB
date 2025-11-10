package com.smartfitness.auth.internal.logic;

import com.smartfitness.auth.model.AuthToken;
import com.smartfitness.auth.model.RegistrationDetails;
import com.smartfitness.auth.model.UserCredentials;
import com.smartfitness.auth.ports.IAuthRepository;
import com.smartfitness.auth.ports.IAuthServiceApi;
import com.smartfitness.auth.ports.ICreditCardVerificationService;
import com.smartfitness.event.UserRegisteredEvent;
import com.smartfitness.messaging.IMessagePublisherService;

import com.smartfitness.domain.model.UserAccount;

import java.util.Optional;

/**
 * AuthorizationManager: Core auth flows - login, token validation, registration.
 */
public class AuthorizationManager implements IAuthServiceApi {
    private final IAuthRepository repository;
    private final ICreditCardVerificationService verificationClient;
    private final IMessagePublisherService messagePublisher;

    public AuthorizationManager(IAuthRepository repository,
                                ICreditCardVerificationService verificationClient,
                                IMessagePublisherService messagePublisher) {
        this.repository = repository;
        this.verificationClient = verificationClient;
        this.messagePublisher = messagePublisher;
    }

    @Override
    public AuthToken login(UserCredentials credentials) {
        Optional<UserAccount> userOpt = repository.findByUsername(credentials.getUsername());
        if (userOpt.isEmpty()) {
            throw new SecurityException("User not found");
        }
        String storedHash = repository.loadPasswordHash(userOpt.get().getUserId());
        if (!TokenService.verifyPassword(credentials.getPassword(), storedHash)) {
            throw new SecurityException("Invalid credentials");
        }
        return TokenService.issueToken(userOpt.get());
    }

    @Override
    public boolean validateToken(String token) {
        return TokenService.validate(token);
    }

    @Override
    public void registerUser(RegistrationDetails details) {
        if (!verificationClient.verifyIdentity(details.getCardDetails(), details.getUserId())) {
            throw new SecurityException("Identity verification failed.");
        }

        repository.saveUser(details.toUserAccount());
        messagePublisher.publishEvent("users", new UserRegisteredEvent(details.getUserId()));
    }
}
