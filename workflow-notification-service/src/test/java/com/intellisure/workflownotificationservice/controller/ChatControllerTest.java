package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.*;
import com.intellisure.workflownotificationservice.entity.ChatChannelType;
import com.intellisure.workflownotificationservice.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void getChannelsDelegatesToService() {
        ChatChannelResponse channel = new ChatChannelResponse(
                UUID.randomUUID(), "General", ChatChannelType.INTERNAL_GROUP,
                null, null, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), null, 0L
        );
        when(chatService.getChannelsForCaller()).thenReturn(Flux.just(channel));

        StepVerifier.create(chatController.getChannels())
                .expectNext(channel)
                .verifyComplete();

        verify(chatService).getChannelsForCaller();
    }

    @Test
    void createChannelDelegatesToService() {
        CreateChannelRequest request = new CreateChannelRequest(
                "Underwriting Team", ChatChannelType.INTERNAL_GROUP, null, null, List.of()
        );
        ChatChannelResponse response = new ChatChannelResponse(
                UUID.randomUUID(), "Underwriting Team", ChatChannelType.INTERNAL_GROUP,
                null, null, UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(),
                List.of(), null, 0L
        );
        when(chatService.createOrGetChannel(request)).thenReturn(Mono.just(response));

        StepVerifier.create(chatController.createChannel(request))
                .expectNext(response)
                .verifyComplete();

        verify(chatService).createOrGetChannel(request);
    }

    @Test
    void getMessagesDelegatesToService() {
        UUID channelId = UUID.randomUUID();
        ChatMessageResponse msg = new ChatMessageResponse(
                UUID.randomUUID(), channelId, UUID.randomUUID(), "Alice", "UNDERWRITER", "Hello", LocalDateTime.now()
        );
        when(chatService.getMessages(channelId)).thenReturn(Flux.just(msg));

        StepVerifier.create(chatController.getMessages(channelId))
                .expectNext(msg)
                .verifyComplete();

        verify(chatService).getMessages(channelId);
    }

    @Test
    void sendMessageDelegatesToService() {
        UUID channelId = UUID.randomUUID();
        SendMessageRequest request = new SendMessageRequest("Hi there");
        ChatMessageResponse msg = new ChatMessageResponse(
                UUID.randomUUID(), channelId, UUID.randomUUID(), "Bob", "POLICYHOLDER", "Hi there", LocalDateTime.now()
        );
        when(chatService.sendMessage(channelId, request)).thenReturn(Mono.just(msg));

        StepVerifier.create(chatController.sendMessage(channelId, request))
                .expectNext(msg)
                .verifyComplete();

        verify(chatService).sendMessage(channelId, request);
    }

    @Test
    void streamMessagesDelegatesToService() {
        UUID channelId = UUID.randomUUID();
        ChatMessageResponse msg = new ChatMessageResponse(
                UUID.randomUUID(), channelId, UUID.randomUUID(), "Bob", "POLICYHOLDER", "Hi", LocalDateTime.now()
        );
        ServerSentEvent<ChatMessageResponse> sse = ServerSentEvent.builder(msg).event("message").build();
        when(chatService.streamChannel(channelId)).thenReturn(Flux.just(sse));

        StepVerifier.create(chatController.streamMessages(channelId))
                .expectNext(sse)
                .verifyComplete();

        verify(chatService).streamChannel(channelId);
    }

    @Test
    void getContactsDelegatesToService() {
        ChatContactResponse contact = new ChatContactResponse(
                UUID.randomUUID(), "John Doe", "john@example.com", "UNDERWRITER", "Assigned Underwriter", null, null
        );
        when(chatService.getContactsForCaller()).thenReturn(Flux.just(contact));

        StepVerifier.create(chatController.getContacts())
                .expectNext(contact)
                .verifyComplete();

        verify(chatService).getContactsForCaller();
    }
}
