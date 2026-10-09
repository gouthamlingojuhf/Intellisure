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
}
