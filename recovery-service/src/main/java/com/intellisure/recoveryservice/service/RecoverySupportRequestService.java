package com.intellisure.recoveryservice.service;

import com.intellisure.recoveryservice.dto.CreateRecoverySupportRequest;
import com.intellisure.recoveryservice.dto.RecoverySupportRequestResponse;
import com.intellisure.recoveryservice.dto.UpdateSupportRequestStatusRequest;
import com.intellisure.recoveryservice.entity.RecoverySupportRequest;
import com.intellisure.recoveryservice.entity.RecoverySupportStatus;
import com.intellisure.recoveryservice.entity.SupportPriority;
import com.intellisure.recoveryservice.entity.SupportType;
import com.intellisure.recoveryservice.repository.RecoveryCaseRepository;
import com.intellisure.recoveryservice.repository.RecoverySupportRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecoverySupportRequestService {

    private final RecoverySupportRequestRepository supportRequestRepository;
    private final RecoveryCaseRepository recoveryCaseRepository;

    public Mono<RecoverySupportRequestResponse> createSupportRequest(CreateRecoverySupportRequest request) {
        return recoveryCaseRepository.findById(request.recoveryCaseId())
                .flatMap(recoveryCase -> {
                    RecoverySupportRequest supportRequest = RecoverySupportRequest.builder()
                            .supportRequestId(UUID.randomUUID())
                            .recoveryCaseId(request.recoveryCaseId())
                            .supportType(request.supportType())
                            .description(request.description())
                            .priority(request.priority())
                            .status(com.intellisure.recoveryservice.entity.RecoverySupportStatus.PENDING)
                            .requiredByDate(request.requiredByDate())
                            .location(request.location())
                            .vendorAssignmentId(request.vendorAssignmentId())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();

                    return supportRequestRepository.save(supportRequest)
                            .map(this::mapToResponse);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Recovery case not found: " + request.recoveryCaseId())));
    }

    public Mono<RecoverySupportRequestResponse> getSupportRequest(UUID supportRequestId) {
        return supportRequestRepository.findById(supportRequestId)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Support request not found: " + supportRequestId)));
    }

    public Flux<RecoverySupportRequestResponse> getSupportRequests(UUID recoveryCaseId) {
        return supportRequestRepository.findByRecoveryCaseId(recoveryCaseId)
                .map(this::mapToResponse);
    }

    public Flux<RecoverySupportRequestResponse> getSupportRequestsByStatus(com.intellisure.recoveryservice.entity.RecoverySupportStatus status) {
        return supportRequestRepository.findByStatus(status)
                .map(this::mapToResponse);
    }

    public Flux<RecoverySupportRequestResponse> getSupportRequestsByPriority(com.intellisure.recoveryservice.entity.SupportPriority priority) {
        return supportRequestRepository.findByPriority(priority)
                .map(this::mapToResponse);
    }

    public Flux<RecoverySupportRequestResponse> getSupportRequestsByType(com.intellisure.recoveryservice.entity.SupportType supportType) {
        return supportRequestRepository.findBySupportType(supportType)
                .map(this::mapToResponse);
    }

    public Flux<RecoverySupportRequestResponse> getOverdueSupportRequests() {
        return supportRequestRepository.findByRequiredByDateBefore(LocalDate.now())
                .map(this::mapToResponse);
    }

    public Mono<RecoverySupportRequestResponse> updateStatus(UUID supportRequestId, UpdateSupportRequestStatusRequest request) {
        return supportRequestRepository.findById(supportRequestId)
                .flatMap(supportRequest -> {
                    supportRequest.setStatus(request.status());
                    supportRequest.setUpdatedAt(LocalDateTime.now());
                    supportRequest.setNew(false);
                    return supportRequestRepository.save(supportRequest);
                })
                .map(this::mapToResponse);
    }

    private RecoverySupportRequestResponse mapToResponse(RecoverySupportRequest request) {
        return new RecoverySupportRequestResponse(
                request.getSupportRequestId(),
                request.getRecoveryCaseId(),
                request.getSupportType(),
                request.getDescription(),
                request.getPriority(),
                request.getStatus(),
                request.getRequiredByDate(),
                request.getLocation(),
                request.getVendorAssignmentId(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}