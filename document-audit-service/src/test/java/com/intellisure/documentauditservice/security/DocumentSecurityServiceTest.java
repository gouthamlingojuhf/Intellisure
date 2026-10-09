package com.intellisure.documentauditservice.security;

import com.intellisure.documentauditservice.client.DocumentEntityOwnershipClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DocumentSecurityServiceTest {
    private final SecurityActorService actorService = mock(SecurityActorService.class);
    private final DocumentEntityOwnershipClient ownershipClient = mock(DocumentEntityOwnershipClient.class);
    private final DocumentSecurityService service = new DocumentSecurityService(actorService, ownershipClient);

    @Test
    void staffAccessBypassesCustomerOwnershipLookup() {
        when(actorService.hasAnyDocumentStaffRole()).thenReturn(Mono.just(true));

        StepVerifier.create(service.assertEntityAccess(UUID.randomUUID(), "CLAIM"))
                .verifyComplete();
        verifyNoInteractions(ownershipClient);
    }

    @Test
    void nonStaffAccessIsDeniedWhenOwnershipCannotBeResolved() {
        UUID customerId = UUID.randomUUID();
        UUID entityId = UUID.randomUUID();
        when(actorService.hasAnyDocumentStaffRole()).thenReturn(Mono.just(false));
        when(actorService.currentCustomerId()).thenReturn(Mono.just(customerId));
        when(ownershipClient.findCustomerId(entityId, "CLAIM")).thenReturn(Mono.empty());

        StepVerifier.create(service.assertEntityAccess(entityId, "CLAIM"))
                .expectErrorMessage("You are not authorized to access this document entity")
                .verify();
    }

    @Test
    void exposesCurrentUserIdFromActorService() {
        UUID userId = UUID.randomUUID();
        when(actorService.currentUserId()).thenReturn(Mono.just(userId));

        StepVerifier.create(service.currentUserId())
                .expectNext(userId).verifyComplete();
    }
}
