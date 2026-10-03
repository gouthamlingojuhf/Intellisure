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

@Service
@RequiredArgsConstructor
public class VendorAssignmentService {

    private final VendorAssignmentRepository assignmentRepository;
    private final VendorRepository vendorRepository;

    public Mono<VendorAssignmentResponse> createAssignment(CreateVendorAssignmentRequest request) {
        return vendorRepository.findById(request.vendorId())
                .flatMap(vendor -> {
                    if (vendor.getVerificationStatus() != VendorVerificationStatus.VERIFIED ||
                        vendor.getActiveStatus() != VendorActiveStatus.ACTIVE) {
                        return Mono.error(new IllegalStateException("Vendor must be verified and active for assignment"));
                    }
                    
                    VendorAssignment assignment = VendorAssignment.builder()
                            .assignmentId(UUID.randomUUID())
                            .vendorId(request.vendorId())
                            .assignmentType(AssignmentType.valueOf(request.assignmentType()))
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
                .map(this::mapToResponse);
    }

    public Mono<VendorAssignmentResponse> acceptAssignment(UUID assignmentId, AcceptAssignmentRequest request) {
        return assignmentRepository.findById(assignmentId)
                .flatMap(assignment -> {
                    if (assignment.getStatus() != AssignmentStatus.DISPATCHED && 
                        assignment.getStatus() != AssignmentStatus.OFFERED) {
                        return Mono.error(new IllegalStateException("Assignment must be in DISPATCHED or OFFERED status to accept"));
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
                .flatMap(assignment -> {
                    if (assignment.getStatus() != AssignmentStatus.DISPATCHED && 
                        assignment.getStatus() != AssignmentStatus.OFFERED) {
                        return Mono.error(new IllegalStateException("Assignment must be in DISPATCHED or OFFERED status to decline"));
                    }
                    assignment.setStatus(AssignmentStatus.DECLINED);
                    assignment.setUpdatedAt(LocalDateTime.now());
                    assignment.setNew(false);
                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    public Mono<VendorAssignmentResponse> updateAssignmentStatus(UUID assignmentId, UpdateAssignmentStatusRequest request) {
        return assignmentRepository.findById(assignmentId)
                .flatMap(assignment -> {
                    AssignmentStatus newStatus = AssignmentStatus.valueOf(request.status());
                    
                    if (newStatus == AssignmentStatus.COMPLETED) {
                        if (assignment.getStatus() != AssignmentStatus.ACCEPTED &&
                            assignment.getStatus() != AssignmentStatus.IN_PROGRESS) {
                            return Mono.error(new IllegalStateException("Assignment must be ACCEPTED or IN_PROGRESS to complete"));
                        }
                        assignment.setCompletedAt(LocalDateTime.now());
                        assignment.setEvidenceDocumentIds(request.evidenceDocumentIds());
                    } else if (newStatus == AssignmentStatus.IN_PROGRESS) {
                        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) {
                            return Mono.error(new IllegalStateException("Assignment must be ACCEPTED to start work"));
                        }
                    }
                    
                    assignment.setStatus(newStatus);
                    assignment.setUpdatedAt(LocalDateTime.now());
                    assignment.setNew(false);
                    return assignmentRepository.save(assignment)
                            .map(this::mapToResponse);
                });
    }

    private VendorAssignmentResponse mapToResponse(VendorAssignment entity) {
        return new VendorAssignmentResponse(
                entity.getAssignmentId(),
                entity.getVendorId(),
                entity.getAssignmentType().name(),
                entity.getClaimId(),
                entity.getRecoveryCaseId(),
                entity.getStatus().name(),
                entity.getTaskDescription(),
                entity.getDueDate(),
                entity.getPriority(),
                entity.getAcceptedAt(),
                entity.getCompletedAt(),
                entity.getEvidenceDocumentIds(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}