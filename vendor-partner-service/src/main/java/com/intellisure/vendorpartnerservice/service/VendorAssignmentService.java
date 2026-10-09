package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.dto.AcceptAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.CreateVendorAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.DeclineAssignmentRequest;
import com.intellisure.vendorpartnerservice.dto.UpdateAssignmentStatusRequest;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentFilterRequest;
import com.intellisure.vendorpartnerservice.dto.VendorAssignmentResponse;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.AssignmentType;
import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.repository.VendorAssignmentRepository;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VendorAssignmentService {

    private final VendorAssignmentRepository assignmentRepository;
    private final VendorRepository vendorRepository;

    public Mono<VendorAssignmentResponse> createAssignment(CreateVendorAssignmentRequest request) {
        if (request.recoveryPath() != null) {
            String path = request.recoveryPath().trim().toUpperCase();
            if ("CUSTOMER_VENDOR".equals(path) || "CUSTOMER_MANAGED".equals(path)) {
                return Mono.error(new IllegalStateException(
                        "Vendor assignment is not permitted for customer-owned recovery path: " + path));
            }
            if (!"NETWORK_VENDOR".equals(path)) {
                return Mono.error(new IllegalArgumentException("Invalid recovery path: " + request.recoveryPath()));
            }
        }

        return vendorRepository.findById(request.vendorId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Vendor not found: " + request.vendorId())))
                .flatMap(vendor -> {
                    if (vendor.getVerificationStatus() != VendorVerificationStatus.VERIFIED ||
                        vendor.getActiveStatus() != VendorActiveStatus.ACTIVE) {
                        return Mono.error(new IllegalStateException("Vendor must be verified and active for assignment"));
                    }

                    AssignmentType type = parseAssignmentType(request.assignmentType());
                    
                    VendorAssignment assignment = VendorAssignment.builder()
                            .assignmentId(UUID.randomUUID())
                            .vendorId(request.vendorId())
                            .assignmentType(type)
                            .claimId(request.claimId())
                            .recoveryCaseId(request.recoveryCaseId())
                            .status(AssignmentStatus.DISPATCHED)
                            .taskDescription(request.taskDescription())
                            .dueDate(request.dueDate())
                            .priority(request.priority())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();

                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    public Flux<VendorAssignmentResponse> getAssignments(VendorAssignmentFilterRequest filter) {
        if (filter.vendorId() != null) {
            return assignmentRepository.findByVendorId(filter.vendorId())
                    .map(this::mapToResponse);
        }
        if (filter.claimId() != null) {
            return assignmentRepository.findByClaimId(filter.claimId())
                    .map(this::mapToResponse);
        }
        if (filter.recoveryCaseId() != null) {
            return assignmentRepository.findByRecoveryCaseId(filter.recoveryCaseId())
                    .map(this::mapToResponse);
        }
        if (filter.status() != null) {
            return assignmentRepository.findByStatus(AssignmentStatus.valueOf(filter.status()))
                    .map(this::mapToResponse);
        }
        return assignmentRepository.findAll()
                .map(this::mapToResponse);
    }

    public Mono<VendorAssignmentResponse> getAssignment(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment not found: " + assignmentId)))
                .map(this::mapToResponse);
    }

    public Mono<VendorAssignmentResponse> acceptAssignment(UUID assignmentId, AcceptAssignmentRequest request) {
        return assignmentRepository.findById(assignmentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment not found: " + assignmentId)))
                .flatMap(assignment -> {
                    try {
                        validateStatusTransition(assignment.getStatus(), AssignmentStatus.ACCEPTED);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        return Mono.error(e);
                    }
                    assignment.setStatus(AssignmentStatus.ACCEPTED);
                    assignment.setAcceptedAt(LocalDateTime.now());
                    assignment.setUpdatedAt(LocalDateTime.now());
                    assignment.setNew(false);
                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    public Mono<VendorAssignmentResponse> declineAssignment(UUID assignmentId, DeclineAssignmentRequest request) {
        return assignmentRepository.findById(assignmentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment not found: " + assignmentId)))
                .flatMap(assignment -> {
                    try {
                        validateStatusTransition(assignment.getStatus(), AssignmentStatus.DECLINED);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        return Mono.error(e);
                    }
                    assignment.setStatus(AssignmentStatus.DECLINED);
                    assignment.setUpdatedAt(LocalDateTime.now());
                    assignment.setNew(false);
                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    public Mono<VendorAssignmentResponse> updateAssignmentStatus(UUID assignmentId, UpdateAssignmentStatusRequest request) {
        if (request == null || request.status() == null) {
            return Mono.error(new IllegalArgumentException("Status cannot be null"));
        }
        return assignmentRepository.findById(assignmentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Assignment not found: " + assignmentId)))
                .flatMap(assignment -> {
                    AssignmentStatus newStatus;
                    try {
                        newStatus = AssignmentStatus.valueOf(request.status().trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new IllegalArgumentException("Invalid assignment status: " + request.status()));
                    }

                    try {
                        validateStatusTransition(assignment.getStatus(), newStatus);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        return Mono.error(e);
                    }
                    
                    if (newStatus == AssignmentStatus.COMPLETED) {
                        assignment.setCompletedAt(LocalDateTime.now());
                        if (request.evidenceDocumentIds() != null) {
                            assignment.setEvidenceDocumentIds(
                                    request.evidenceDocumentIds().stream()
                                            .filter(Objects::nonNull)
                                            .map(UUID::toString)
                                            .toList()
                            );
                        }
                    }
                    
                    assignment.setStatus(newStatus);
                    assignment.setUpdatedAt(LocalDateTime.now());
                    assignment.setNew(false);
                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    public void validateStatusTransition(AssignmentStatus currentStatus, AssignmentStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new IllegalArgumentException("Current and target status must not be null");
        }
        if (currentStatus == targetStatus) {
            return;
        }
        if (currentStatus == AssignmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot transition a COMPLETED assignment to " + targetStatus);
        }
        if (currentStatus == AssignmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transition a CANCELLED assignment to " + targetStatus);
        }
        if (currentStatus == AssignmentStatus.DECLINED) {
            throw new IllegalStateException("Cannot transition a DECLINED assignment to " + targetStatus);
        }
        if (targetStatus == AssignmentStatus.IN_PROGRESS && currentStatus != AssignmentStatus.ACCEPTED) {
            throw new IllegalStateException("Assignment must be ACCEPTED to start work");
        }
        if (targetStatus == AssignmentStatus.COMPLETED &&
                currentStatus != AssignmentStatus.ACCEPTED && currentStatus != AssignmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Assignment must be ACCEPTED or IN_PROGRESS to complete");
        }
        if (targetStatus == AssignmentStatus.ACCEPTED &&
                currentStatus != AssignmentStatus.DISPATCHED &&
                currentStatus != AssignmentStatus.OFFERED &&
                currentStatus != AssignmentStatus.ASSIGNED &&
                currentStatus != AssignmentStatus.PENDING &&
                currentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("Assignment must be in DISPATCHED or OFFERED status to accept");
        }
        if (targetStatus == AssignmentStatus.DECLINED &&
                currentStatus != AssignmentStatus.DISPATCHED &&
                currentStatus != AssignmentStatus.OFFERED &&
                currentStatus != AssignmentStatus.ASSIGNED &&
                currentStatus != AssignmentStatus.PENDING &&
                currentStatus != AssignmentStatus.REQUESTED) {
            throw new IllegalStateException("Assignment must be in DISPATCHED or OFFERED status to decline");
        }
    }

    private AssignmentType parseAssignmentType(String typeStr) {
        if (typeStr == null || typeStr.isBlank()) {
            return AssignmentType.OTHER;
        }
        try {
            return AssignmentType.valueOf(typeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AssignmentType.OTHER;
        }
    }

    private VendorAssignmentResponse mapToResponse(VendorAssignment entity) {
        return new VendorAssignmentResponse(
                entity.getAssignmentId(),
                entity.getVendorId(),
                entity.getAssignmentType() != null ? entity.getAssignmentType().name() : null,
                entity.getClaimId(),
                entity.getRecoveryCaseId(),
                entity.getStatus() != null ? entity.getStatus().name() : null,
                entity.getTaskDescription(),
                entity.getDueDate(),
                entity.getPriority(),
                entity.getAcceptedAt(),
                entity.getCompletedAt(),
                parseEvidenceDocumentIds(entity.getEvidenceDocumentIds()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private List<UUID> parseEvidenceDocumentIds(List<String> evidenceDocumentIds) {
        if (evidenceDocumentIds == null) {
            return List.of();
        }
        return evidenceDocumentIds.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(UUID::fromString)
                .toList();
    }
}
