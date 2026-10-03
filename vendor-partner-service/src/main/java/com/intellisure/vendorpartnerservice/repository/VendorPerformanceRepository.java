package com.intellisure.vendorpartnerservice.repository;

import com.intellisure.vendorpartnerservice.entity.VendorPerformance;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface VendorPerformanceRepository extends R2dbcRepository<VendorPerformance, UUID> {
    Flux<VendorPerformance> findByVendorId(UUID vendorId);
    Flux<VendorPerformance> findByAssignmentId(UUID assignmentId);
    Flux<VendorPerformance> findByVendorIdAndRecordedAtBetween(UUID vendorId, LocalDateTime start, LocalDateTime end);
    Mono<VendorPerformance> findByAssignmentIdAndVendorId(UUID assignmentId, UUID vendorId);
}