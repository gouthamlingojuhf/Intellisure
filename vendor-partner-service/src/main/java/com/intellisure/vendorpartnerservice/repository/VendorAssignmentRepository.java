package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.VendorAssignment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface VendorAssignmentRepository extends R2dbcRepository<VendorAssignment, UUID> {
    Flux<VendorAssignment> findByVendorId(UUID vendorId);
    Flux<VendorAssignment> findByClaimId(UUID claimId);
}
