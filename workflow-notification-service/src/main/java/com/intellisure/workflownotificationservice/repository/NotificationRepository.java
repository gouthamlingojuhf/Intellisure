package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.Notification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {
    Flux<Notification> findByRecipientId(UUID recipientId);
}
