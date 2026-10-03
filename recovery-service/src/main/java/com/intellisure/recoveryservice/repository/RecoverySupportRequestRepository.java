package com.intellisure.recoveryservice.repository;

import com.intellisure.recoveryservice.entity.RecoverySupportRequest;
import com.intellisure.recoveryservice.entity.RecoverySupportStatus;
import com.intellisure.recoveryservice.entity.SupportPriority;
import com.intellisure.recoveryservice.entity.SupportType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

public interface RecoverySupportRequestRepository extends R2dbcRepository<RecoverySupportRequest, UUID> {
    Flux<RecoverySupportRequest> findByRecoveryCaseId(UUID recoveryCaseId);
    Flux<RecoverySupportRequest> findByStatus(RecoverySupportStatus status);
    Flux<RecoverySupportRequest> findByPriority(SupportPriority priority);
    Flux<RecoverySupportRequest> findBySupportType(SupportType supportType);
    Flux<RecoverySupportRequest> findByRequiredByDateBefore(LocalDate requiredByDate);
}