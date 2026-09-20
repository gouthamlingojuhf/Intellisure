package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.NotificationResponse;
import com.intellisure.workflownotificationservice.dto.SendNotificationRequest;
import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Flux<NotificationResponse> getNotifications(UUID recipientId) {
        return notificationRepository.findByRecipientId(recipientId).map(this::mapToResponse);
    }

    public Mono<NotificationResponse> markRead(UUID id) {
        return notificationRepository.findById(id).flatMap(n -> {
            n.setStatus("READ"); n.setReadAt(LocalDateTime.now()); n.setNew(false);
            return notificationRepository.save(n);
        }).map(this::mapToResponse);
    }

    public Mono<NotificationResponse> sendNotification(SendNotificationRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .recipientId(request.recipientId())
                .title(request.title())
                .message(request.message())
                .status("SENT")
                .channel(request.channel())
                .createdAt(now)
                .isNew(true)
                .build();

        return notificationRepository.save(notification)
                .map(this::mapToResponse);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getRecipientId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getChannel(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
