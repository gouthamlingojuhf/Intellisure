package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {
    Flux<Notification> findByUserId(UUID userId);
    Flux<Notification> findByUserIdAndRead(UUID userId, boolean read);
    Flux<Notification> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
    Flux<Notification> findByType(NotificationType type);
    Flux<Notification> findByUserIdAndCreatedAtAfter(UUID userId, LocalDateTime after);
    Mono<Long> countByUserIdAndRead(UUID userId, boolean read);
}