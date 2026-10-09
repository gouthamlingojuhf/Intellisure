package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoverySupportRequest;
import com.intellisure.recoveryservice.dto.UpdateSupportRequestStatusRequest;
import com.intellisure.recoveryservice.entity.*;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.repository.RecoverySupportRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecoverySupportRequestServiceTest {
    @Mock RecoverySupportRequestRepository repository;
    @Mock RecoveryCaseRepository caseRepository;
    @InjectMocks RecoverySupportRequestService service;

    @Test
    void createsUpdatesAndQueriesSupportRequests() {
        UUID caseId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        RecoverySupportRequest entity = request(id, caseId);
        when(caseRepository.findById(caseId)).thenReturn(Mono.just(RecoveryCase.builder().recoveryCaseId(caseId).build()));
        when(repository.save(any(RecoverySupportRequest.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(repository.findById(id)).thenReturn(Mono.just(entity));
        when(repository.findByRecoveryCaseId(caseId)).thenReturn(Flux.just(entity));
        when(repository.findByStatus(RecoverySupportStatus.PENDING)).thenReturn(Flux.just(entity));
        when(repository.findByPriority(SupportPriority.HIGH)).thenReturn(Flux.just(entity));
        when(repository.findBySupportType(SupportType.REPAIR)).thenReturn(Flux.just(entity));
        when(repository.findByRequiredByDateBefore(any())).thenReturn(Flux.just(entity));

        StepVerifier.create(service.createSupportRequest(new CreateRecoverySupportRequest(caseId, SupportType.REPAIR,
                        "Repair", SupportPriority.HIGH, LocalDate.now().plusDays(2), "Site", null)))
                .assertNext(response -> assertEquals(RecoverySupportStatus.PENDING, response.status())).verifyComplete();
        StepVerifier.create(service.getSupportRequest(id)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getSupportRequests(caseId)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getSupportRequestsByStatus(RecoverySupportStatus.PENDING)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getSupportRequestsByPriority(SupportPriority.HIGH)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getSupportRequestsByType(SupportType.REPAIR)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getOverdueSupportRequests()).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.updateStatus(id, new UpdateSupportRequestStatusRequest(RecoverySupportStatus.IN_PROGRESS)))
                .assertNext(response -> assertEquals(RecoverySupportStatus.IN_PROGRESS, response.status())).verifyComplete();
    }

    @Test
    void reportsMissingCaseAndSupportRequest() {
        UUID caseId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(caseRepository.findById(caseId)).thenReturn(Mono.empty());
        when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.createSupportRequest(new CreateRecoverySupportRequest(caseId, SupportType.REPAIR,
                        "Repair", SupportPriority.HIGH, LocalDate.now(), "Site", null)))
                .expectError(IllegalArgumentException.class).verify();
        StepVerifier.create(service.getSupportRequest(id)).expectError(IllegalArgumentException.class).verify();
    }

    private RecoverySupportRequest request(UUID id, UUID caseId) {
        return RecoverySupportRequest.builder().supportRequestId(id).recoveryCaseId(caseId).supportType(SupportType.REPAIR)
                .description("Repair").priority(SupportPriority.HIGH).status(RecoverySupportStatus.PENDING)
                .requiredByDate(LocalDate.now()).location("Site").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).isNew(false).build();
    }
}
