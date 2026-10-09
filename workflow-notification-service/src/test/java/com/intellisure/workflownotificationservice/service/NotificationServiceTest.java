package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.CreateNotificationRequest;
import com.intellisure.workflownotificationservice.dto.MarkNotificationReadRequest;
import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.entity.NotificationChannel;
import com.intellisure.workflownotificationservice.entity.NotificationType;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import com.intellisure.workflownotificationservice.security.SecurityActorService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @Mock AsyncNotificationDispatchService asyncDispatchService;
    @Mock SecurityActorService securityActorService;
    @InjectMocks NotificationService service;

    @BeforeEach
    void allowExistingServiceTestsToActAsAuthenticatedUsers() {
        lenient().when(securityActorService.assertUserAccess(any(UUID.class))).thenReturn(Mono.empty());
        lenient().when(securityActorService.assertNotificationCreationAccess(any(UUID.class))).thenReturn(Mono.empty());
    }

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

    @Test
    void duplicateNotificationRequestSuppressesDuplicateAndReturnsExisting() {
        UUID recipient = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        Notification existing = Notification.builder().notificationId(UUID.randomUUID()).userId(recipient)
                .title("Quote ready").message("Review your quote").channel(NotificationChannel.IN_APP)
                .type(NotificationType.QUOTE_SUBMITTED).referenceType("QUOTE").referenceId(referenceId)
                .read(false).createdAt(java.time.LocalDateTime.now()).isNew(false).build();

        when(repository.findByUserIdAndTypeAndReferenceTypeAndReferenceId(recipient, NotificationType.QUOTE_SUBMITTED, "QUOTE", referenceId))
                .thenReturn(reactor.core.publisher.Flux.just(existing));

        CreateNotificationRequest request = new CreateNotificationRequest(
                recipient, "QUOTE_SUBMITTED", "Quote ready", "Review your quote", "QUOTE", referenceId, "IN_APP");

        StepVerifier.create(service.createNotification(request))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(existing.getNotificationId(), result.notificationId());
                    org.junit.jupiter.api.Assertions.assertEquals(recipient, result.userId());
                })
                .verifyComplete();

        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void createsInAppNotificationWhenChannelIsMissing() {
        UUID recipient = UUID.randomUUID();
        Notification saved = notification(recipient, NotificationChannel.IN_APP, NotificationType.GENERAL_ALERT);
        when(repository.save(any(Notification.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createNotification(new CreateNotificationRequest(
                        recipient, "GENERAL_ALERT", "Title", "Message", null, null, null)))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals("IN_APP", response.channel()))
                .verifyComplete();
        verify(asyncDispatchService, never()).sendEmailAsync(any(), any(), any(), any(), any());
    }

    @Test
    void createsSmsPushAndWebhookNotificationsThroughDispatchService() {
        UUID recipient = UUID.randomUUID();
        for (String channel : new String[] {"SMS", "PUSH", "WEBHOOK"}) {
            Notification saved = notification(recipient, NotificationChannel.valueOf(channel), NotificationType.GENERAL_ALERT);
            when(repository.save(any(Notification.class))).thenReturn(Mono.just(saved));
            lenient().when(asyncDispatchService.sendSmsAsync(any(), any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));
            lenient().when(asyncDispatchService.sendPushAsync(any(), any(), any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));
            lenient().when(asyncDispatchService.sendWebhookAsync(any(), any(), any(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            StepVerifier.create(service.createNotification(new CreateNotificationRequest(
                            recipient, "GENERAL_ALERT", "Title", "Message", "CLAIM", UUID.randomUUID(), channel)))
                    .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(channel, response.channel()))
                    .verifyComplete();
        }
        verify(asyncDispatchService, times(1)).sendSmsAsync(any(), any(), any(), any());
        verify(asyncDispatchService, times(1)).sendPushAsync(any(), any(), any(), any(), any());
        verify(asyncDispatchService, times(1)).sendWebhookAsync(any(), any(), any(), any());
    }

    @Test
    void listsByTypeAndUnreadCount() {
        UUID recipient = UUID.randomUUID();
        Notification matching = notification(recipient, NotificationChannel.IN_APP, NotificationType.QUOTE_SUBMITTED);
        Notification other = notification(recipient, NotificationChannel.IN_APP, NotificationType.CLAIM_REJECTED);
        when(repository.findByUserId(recipient)).thenReturn(reactor.core.publisher.Flux.just(matching, other));
        when(repository.countByUserIdAndRead(recipient, false)).thenReturn(Mono.just(2L));

        StepVerifier.create(service.getNotificationsByType(recipient, NotificationType.QUOTE_SUBMITTED, 1, 5))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.items().size());
                    org.junit.jupiter.api.Assertions.assertEquals(1, result.page());
                }).verifyComplete();
        StepVerifier.create(service.getUnreadCount(recipient))
                .expectNext(2L).verifyComplete();
    }

    @Test
    void marksMissingNotificationAsError() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.markRead(new MarkNotificationReadRequest(id)))
                .expectErrorMatches(error -> error instanceof IllegalArgumentException
                        && error.getMessage().contains(id.toString()))
                .verify();
    }

    @Test
    void delegatesAccessChecksForNotificationQueries() {
        UUID recipient = UUID.randomUUID();
        when(securityActorService.assertUserAccess(recipient))
                .thenReturn(Mono.error(new SecurityException("denied")));
        when(repository.countByUserIdAndRead(recipient, false)).thenReturn(Mono.just(0L));

        StepVerifier.create(service.getNotifications(recipient, true, 0, 20))
                .expectError(SecurityException.class).verify();
        StepVerifier.create(service.getNotificationsByType(recipient, NotificationType.GENERAL_ALERT, 0, 20))
                .expectError(SecurityException.class).verify();
        StepVerifier.create(service.getUnreadCount(recipient))
                .expectError(SecurityException.class).verify();
    }

    private Notification notification(UUID userId, NotificationChannel channel, NotificationType type) {
        return Notification.builder().notificationId(UUID.randomUUID()).userId(userId)
                .title("Title").message("Message").channel(channel).type(type)
                .read(false).createdAt(java.time.LocalDateTime.now()).isNew(true).build();
    }
}
