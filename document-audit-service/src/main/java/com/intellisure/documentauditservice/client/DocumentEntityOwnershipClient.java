package com.intellisure.documentauditservice.client;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DocumentEntityOwnershipClient {
    Mono<UUID> findCustomerId(UUID entityId, String entityType);
}
