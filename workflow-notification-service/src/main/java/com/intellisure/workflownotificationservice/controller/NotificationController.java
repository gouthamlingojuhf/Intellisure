package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.CreateNotificationRequest;
import com.intellisure.workflownotificationservice.dto.MarkNotificationReadRequest;
import com.intellisure.workflownotificationservice.dto.NotificationListResponse;
import com.intellisure.workflownotificationservice.dto.NotificationResponse;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final com.intellisure.workflownotificationservice.security.SecurityActorService securityActorService;

    @PostMapping
    public Mono<NotificationResponse> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        return notificationService.createNotification(request);
    }

    @GetMapping
    public Mono<NotificationListResponse> getNotifications(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        if (userId != null) {
            return notificationService.getNotifications(userId, read, page, size);
        }
        return securityActorService.currentUserId()
                .flatMap(targetId -> notificationService.getNotifications(targetId, read, page, size));
    }

    @GetMapping("/unread-count")
    public Mono<Long> getUnreadCount(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return notificationService.getUnreadCount(userId);
        }
        return securityActorService.currentUserId()
                .flatMap(notificationService::getUnreadCount);
    }

    @GetMapping("/type/{type}")
    public Mono<NotificationListResponse> getNotificationsByType(
            @RequestParam UUID userId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return notificationService.getNotificationsByType(userId, NotificationType.valueOf(type.toUpperCase()), page, size);
    }

    @PatchMapping("/{notificationId}/read")
    public Mono<NotificationResponse> markRead(
            @PathVariable UUID notificationId,
            @Valid @RequestBody MarkNotificationReadRequest request) {
        return notificationService.markRead(request);
    }

    @PatchMapping("/read")
    public Mono<NotificationResponse> markReadById(@RequestParam UUID notificationId) {
        return notificationService.markRead(new MarkNotificationReadRequest(notificationId));
    }
}