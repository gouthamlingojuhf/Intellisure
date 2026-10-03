package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CreateNotificationRequest;
import com.intellisure.workflownotificationservice.dto.MarkNotificationReadRequest;
import com.intellisure.workflownotificationservice.dto.NotificationListResponse;
import com.intellisure.workflownotificationservice.dto.NotificationResponse;
import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationChannel;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AsyncNotificationDispatchService asyncDispatchService;

    public Mono<NotificationResponse> createNotification(CreateNotificationRequest request) {
        NotificationType type = NotificationType.valueOf(request.type().toUpperCase());
        NotificationChannel channel = request.channel() != null ? 
            NotificationChannel.valueOf(request.channel().toUpperCase()) : NotificationChannel.IN_APP;

        Notification notification = Notification.builder()
            .notificationId(UUID.randomUUID())
            .userId(request.userId())
            .type(type)
            .title(request.title())
            .message(request.message())
            .referenceType(request.referenceType())
            .referenceId(request.referenceId())
            .read(false)
            .channel(channel)
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();

        Mono<NotificationResponse> savedMono = notificationRepository.save(notification)
            .map(this::mapToResponse);

        if (channel == NotificationChannel.EMAIL) {
            return savedMono.flatMap(response -> 
                Mono.fromFuture(asyncDispatchService.sendEmailAsync(
                    request.userId(), request.title(), request.message(), 
                    request.referenceType(), request.referenceId()
                ).thenApply(n -> response))
            );
        } else if (channel == NotificationChannel.SMS) {
            return savedMono.flatMap(response -> 
                Mono.fromFuture(asyncDispatchService.sendSmsAsync(
                    request.userId(), request.message(), 
                    request.referenceType(), request.referenceId()
                ).thenApply(n -> response))
            );
        } else if (channel == NotificationChannel.PUSH) {
            return savedMono.flatMap(response -> 
                Mono.fromFuture(asyncDispatchService.sendPushAsync(
                    request.userId(), request.title(), request.message(), 
                    request.referenceType(), request.referenceId()
                ).thenApply(n -> response))
            );
        } else if (channel == NotificationChannel.WEBHOOK) {
            return savedMono.flatMap(response -> 
                Mono.fromFuture(asyncDispatchService.sendWebhookAsync(
                    request.userId(), request.message(), 
                    request.referenceType(), request.referenceId()
                ).thenApply(n -> response))
            );
        }
        return savedMono;
    }

    public Mono<NotificationResponse> markRead(MarkNotificationReadRequest request) {
        return notificationRepository.findById(request.notificationId())
            .flatMap(notification -> {
                notification.setRead(true);
                notification.setReadAt(LocalDateTime.now());
                notification.setNew(false);
                return notificationRepository.save(notification);
            })
            .map(this::mapToResponse);
    }

    public Mono<NotificationListResponse> getNotifications(UUID userId, boolean read, Integer page, Integer size) {
        Flux<Notification> notifications;
        
        if (read) {
            notifications = notificationRepository.findByUserIdAndRead(userId, true);
        } else {
            notifications = notificationRepository.findByUserIdAndRead(userId, false);
        }
        
        return notifications
            .map(this::mapToResponse)
            .collectList()
            .map(list -> new NotificationListResponse(list, page, size, (long) list.size()));
    }

    public Mono<NotificationListResponse> getNotificationsByType(UUID userId, NotificationType type, Integer page, Integer size) {
        return notificationRepository.findByUserId(userId)
            .filter(n -> n.getType() == type)
            .map(this::mapToResponse)
            .collectList()
            .map(list -> new NotificationListResponse(list, page, size, (long) list.size()));
    }

    public Mono<Long> getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
            notification.getNotificationId(),
            notification.getUserId(),
            notification.getType().name(),
            notification.getTitle(),
            notification.getMessage(),
            notification.getReferenceType(),
            notification.getReferenceId(),
            notification.isRead(),
            notification.getChannel().name(),
            notification.getReadAt(),
            notification.getCreatedAt()
        );
    }
}