package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import com.intellisure.vendorpartnerservice.entity.AssignmentStatus;
import com.intellisure.vendorpartnerservice.entity.AssignmentType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface VendorAssignmentRepository extends R2dbcRepository<VendorAssignment, UUID> {
    Flux<VendorAssignment> findByVendorId(UUID vendorId);
    Flux<VendorAssignment> findByClaimId(UUID claimId);
    Flux<VendorAssignment> findByRecoveryCaseId(UUID recoveryCaseId);
    Flux<VendorAssignment> findByStatus(AssignmentStatus status);
    Flux<VendorAssignment> findByVendorIdAndStatus(UUID vendorId, AssignmentStatus status);
    Flux<VendorAssignment> findByAssignmentType(AssignmentType assignmentType);
    Flux<VendorAssignment> findByDueDateBefore(LocalDate dueDate);
    Mono<VendorAssignment> findByAssignmentIdAndVendorId(UUID assignmentId, UUID vendorId);
}