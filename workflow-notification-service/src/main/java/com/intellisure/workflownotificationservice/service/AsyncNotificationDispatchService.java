package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationChannel;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncNotificationDispatchService {

    private final NotificationRepository notificationRepository;

    @Async("notificationExecutor")
    public CompletableFuture<Notification> sendEmailAsync(UUID userId, String subject, String body, String referenceType, UUID referenceId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending email to user: {} - Subject: {}", userId, subject);
            
            Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.GENERAL_ALERT)
                .title(subject)
                .message(body)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .channel(NotificationChannel.EMAIL)
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();

            return notificationRepository.save(notification)
                .doOnNext(n -> log.debug("Email notification logged: {}", n.getNotificationId()))
                .doOnError(e -> log.error("Failed to log email notification: {}", e.getMessage()))
                .block();
        });
    }

    @Async("notificationExecutor")
    public CompletableFuture<Notification> sendSmsAsync(UUID userId, String message, String referenceType, UUID referenceId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending SMS to user: {}", userId);
            
            Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.GENERAL_ALERT)
                .title("SMS Notification")
                .message(message)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .channel(NotificationChannel.SMS)
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();

            return notificationRepository.save(notification)
                .doOnNext(n -> log.debug("SMS notification logged: {}", n.getNotificationId()))
                .doOnError(e -> log.error("Failed to log SMS notification: {}", e.getMessage()))
                .block();
        });
    }

    @Async("notificationExecutor")
    public CompletableFuture<Notification> sendPushAsync(UUID userId, String title, String body, String referenceType, UUID referenceId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending push notification to user: {}", userId);
            
            Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.GENERAL_ALERT)
                .title(title)
                .message(body)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .channel(NotificationChannel.PUSH)
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();

            return notificationRepository.save(notification)
                .doOnNext(n -> log.debug("Push notification logged: {}", n.getNotificationId()))
                .doOnError(e -> log.error("Failed to log push notification: {}", e.getMessage()))
                .block();
        });
    }

    @Async("notificationExecutor")
    public CompletableFuture<Notification> sendWebhookAsync(UUID userId, String payload, String referenceType, UUID referenceId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Sending webhook to user: {}", userId);
            
            Notification notification = Notification.builder()
                .notificationId(UUID.randomUUID())
                .userId(userId)
                .type(NotificationType.GENERAL_ALERT)
                .title("Webhook Delivery")
                .message(payload)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .read(false)
                .channel(NotificationChannel.WEBHOOK)
                .createdAt(LocalDateTime.now())
                .isNew(true)
                .build();

            return notificationRepository.save(notification)
                .doOnNext(n -> log.debug("Webhook notification logged: {}", n.getNotificationId()))
                .doOnError(e -> log.error("Failed to log webhook notification: {}", e.getMessage()))
                .block();
        });
    }

    public Mono<Notification> createInAppNotification(UUID userId, NotificationType type, String title, String message, String referenceType, UUID referenceId) {
        Notification notification = Notification.builder()
            .notificationId(UUID.randomUUID())
            .userId(userId)
            .type(type)
            .title(title)
            .message(message)
            .referenceType(referenceType)
            .referenceId(referenceId)
            .read(false)
            .channel(NotificationChannel.IN_APP)
            .createdAt(LocalDateTime.now())
            .isNew(true)
            .build();

        return notificationRepository.save(notification);
    }

    public CompletableFuture<Void> dispatchMultiChannel(UUID userId, NotificationType type, String title, String message, String referenceType, UUID referenceId) {
        return CompletableFuture.allOf(
            sendEmailAsync(userId, title, message, referenceType, referenceId).thenAccept(n -> {}),
            sendSmsAsync(userId, message, referenceType, referenceId).thenAccept(n -> {}),
            sendPushAsync(userId, title, message, referenceType, referenceId).thenAccept(n -> {})
        );
    }
}