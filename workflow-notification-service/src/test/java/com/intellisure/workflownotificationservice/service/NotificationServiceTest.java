package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CreateNotificationRequest;
import com.intellisure.workflownotificationservice.dto.MarkNotificationReadRequest;
import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationChannel;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @Mock AsyncNotificationDispatchService asyncDispatchService;
    @InjectMocks NotificationService service;

    @Test
    void creatingNotificationCreatesSentNotification() {
        UUID recipient = UUID.randomUUID();
        Notification saved = Notification.builder().notificationId(UUID.randomUUID()).userId(recipient)
                .title("Quote ready").message("Review your quote").channel(NotificationChannel.EMAIL)
                .type(NotificationType.GENERAL_ALERT).read(false).createdAt(java.time.LocalDateTime.now()).isNew(true).build();
        when(repository.save(any(Notification.class))).thenReturn(Mono.just(saved));
        when(asyncDispatchService.sendEmailAsync(any(), any(), any(), any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        StepVerifier.create(service.createNotification(new CreateNotificationRequest(recipient,
                        "GENERAL_ALERT", "Quote ready", "Review your quote", "QUOTE", UUID.randomUUID(), "EMAIL")))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(recipient, result.userId());
                    org.junit.jupiter.api.Assertions.assertEquals("EMAIL", result.channel());
                }).verifyComplete();
    }

    @Test
    void markingNotificationReadPersistsReadState() {
        UUID id = UUID.randomUUID();
        Notification notification = Notification.builder().notificationId(id).userId(UUID.randomUUID())
                .title("Test").message("Test").channel(NotificationChannel.IN_APP)
                .type(NotificationType.GENERAL_ALERT).read(false).createdAt(java.time.LocalDateTime.now()).isNew(true).build();
        when(repository.findById(id)).thenReturn(Mono.just(notification));
        when(repository.save(notification)).thenReturn(Mono.just(notification));
        StepVerifier.create(service.markRead(new MarkNotificationReadRequest(id)))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(true, result.read()))
                .verifyComplete();
        org.junit.jupiter.api.Assertions.assertNotNull(notification.getReadAt());
    }

    @Test
    void recipientNotificationListingMapsAllNotifications() {
        UUID recipient = UUID.randomUUID();
        Notification notification = Notification.builder().notificationId(UUID.randomUUID())
                .userId(recipient).title("Quote ready").message("Review your quote")
                .channel(NotificationChannel.IN_APP).type(NotificationType.GENERAL_ALERT).read(false).build();
        when(repository.findByUserIdAndRead(recipient, false)).thenReturn(reactor.core.publisher.Flux.just(notification));

        StepVerifier.create(service.getNotifications(recipient, false, 0, 20))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        1, result.items().size()))
                .verifyComplete();
        verify(repository).findByUserIdAndRead(recipient, false);
    }
}