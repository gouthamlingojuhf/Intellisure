package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.NotificationResponse;
import com.intellisure.workflownotificationservice.dto.SendNotificationRequest;
import com.intellisure.workflownotificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public Mono<NotificationResponse> sendNotification(@Valid @RequestBody SendNotificationRequest request) {
        return notificationService.sendNotification(request);
    }

    @GetMapping
    public Flux<NotificationResponse> notifications(@RequestParam UUID recipientId) {
        return notificationService.getNotifications(recipientId);
    }

    @PatchMapping("/{notificationId}/read")
    public Mono<NotificationResponse> read(@PathVariable UUID notificationId) {
        return notificationService.markRead(notificationId);
    }
}
