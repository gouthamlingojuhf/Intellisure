package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.SendNotificationRequest;
import com.intellisure.workflownotificationservice.entity.Notification;
import com.intellisure.workflownotificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock NotificationRepository repository;
    @InjectMocks NotificationService service;

    @Test
    void sendingNotificationCreatesSentNotification() {
        UUID recipient = UUID.randomUUID();
        Notification saved = Notification.builder().notificationId(UUID.randomUUID()).recipientId(recipient)
                .title("Quote ready").message("Review your quote").channel("EMAIL").status("SENT").build();
        when(repository.save(any(Notification.class))).thenReturn(Mono.just(saved));
        StepVerifier.create(service.sendNotification(new SendNotificationRequest(recipient,
                        "Quote ready", "Review your quote", "EMAIL")))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("SENT", result.status());
                    org.junit.jupiter.api.Assertions.assertEquals(recipient, result.recipientId());
                }).verifyComplete();
    }

    @Test
    void markingNotificationReadPersistsReadState() {
        UUID id = UUID.randomUUID();
        Notification notification = Notification.builder().notificationId(id).status("SENT").build();
        when(repository.findById(id)).thenReturn(Mono.just(notification));
        when(repository.save(notification)).thenReturn(Mono.just(notification));
        StepVerifier.create(service.markRead(id))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals("READ", result.status()))
                .verifyComplete();
        org.junit.jupiter.api.Assertions.assertNotNull(notification.getReadAt());
    }

    @Test
    void recipientNotificationListingMapsAllNotifications() {
        UUID recipient = UUID.randomUUID();
        Notification notification = Notification.builder().notificationId(UUID.randomUUID())
                .recipientId(recipient).title("Quote ready").message("Review your quote")
                .status("SENT").channel("EMAIL").build();
        when(repository.findByRecipientId(recipient)).thenReturn(reactor.core.publisher.Flux.just(notification));

        StepVerifier.create(service.getNotifications(recipient))
                .assertNext(result -> org.junit.jupiter.api.Assertions.assertEquals(
                        recipient, result.recipientId()))
                .verifyComplete();
        verify(repository).findByRecipientId(recipient);
    }
}
