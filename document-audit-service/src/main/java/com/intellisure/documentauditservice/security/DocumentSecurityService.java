package com.intellisure.documentauditservice.security;

import com.intellisure.documentauditservice.client.DocumentEntityOwnershipClient;
import com.intellisure.documentauditservice.exception.AccessDeniedBusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentSecurityService {

    private final SecurityActorService securityActorService;
    private final DocumentEntityOwnershipClient ownershipClient;

    public Mono<Void> assertEntityAccess(UUID entityId, String entityType) {
        return securityActorService.hasAnyDocumentStaffRole()
                .flatMap(isStaff -> isStaff
                        ? Mono.empty()
                        : securityActorService.currentCustomerId()
                                .flatMap(customerId -> ownershipClient.findCustomerId(entityId, entityType)
                                        .filter(customerId::equals)
                                        .switchIfEmpty(Mono.error(new AccessDeniedBusinessException(
                                                "You are not authorized to access this document entity")))
                                        .then()));
    }

    public Mono<UUID> currentUserId() {
        return securityActorService.currentUserId();
    }
}
