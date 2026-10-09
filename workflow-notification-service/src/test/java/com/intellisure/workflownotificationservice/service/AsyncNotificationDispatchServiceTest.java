package com.intellisure.workflownotificationservice.service;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncNotificationDispatchServiceTest {

    @Mock NotificationRepository repository;
    @InjectMocks AsyncNotificationDispatchService service;

    @Test
    void dispatchesEmailSmsPushAndWebhookWithCorrectChannels() throws Exception {
        UUID user = UUID.randomUUID();
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        assertEquals(NotificationChannel.EMAIL, service.sendEmailAsync(user, "Subject", "Body", "QUOTE", user).get().getChannel());
        assertEquals(NotificationChannel.SMS, service.sendSmsAsync(user, "Body", "QUOTE", user).get().getChannel());
        assertEquals(NotificationChannel.PUSH, service.sendPushAsync(user, "Title", "Body", "QUOTE", user).get().getChannel());
        assertEquals(NotificationChannel.WEBHOOK, service.sendWebhookAsync(user, "Payload", "QUOTE", user).get().getChannel());
        verify(repository, times(4)).save(any(Notification.class));
    }

    @Test
    void createsInAppNotificationAndDispatchesAllExternalChannels() throws Exception {
        UUID user = UUID.randomUUID();
        when(repository.save(any(Notification.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.createInAppNotification(user, NotificationType.TASK_ASSIGNED,
                        "Assigned", "Review task", "WORKFLOW_TASK", user))
                .assertNext(notification -> {
                    assertEquals(NotificationChannel.IN_APP, notification.getChannel());
                    assertEquals(NotificationType.TASK_ASSIGNED, notification.getType());
                }).verifyComplete();

        service.dispatchMultiChannel(user, NotificationType.GENERAL_ALERT, "Title", "Body", "QUOTE", user).get();
        verify(repository, times(4)).save(any(Notification.class));
    }

    @Test
    void propagatesRepositoryFailureFromAsyncDispatch() {
        when(repository.save(any(Notification.class))).thenReturn(Mono.error(new IllegalStateException("database down")));

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
                () -> service.sendEmailAsync(UUID.randomUUID(), "Subject", "Body", null, null).get());
    }
}
