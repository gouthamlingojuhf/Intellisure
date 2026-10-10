package com.intellisure.workflownotificationservice.controller;

import com.intellisure.workflownotificationservice.dto.*;
import com.intellisure.workflownotificationservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/channels")
    public Flux<ChatChannelResponse> getChannels() {
        return chatService.getChannelsForCaller();
    }

    @PostMapping("/channels")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ChatChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        return chatService.createOrGetChannel(request);
    }

    @GetMapping("/channels/{channelId}/messages")
    public Flux<ChatMessageResponse> getMessages(@PathVariable UUID channelId) {
        return chatService.getMessages(channelId);
    }

    @PostMapping("/channels/{channelId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ChatMessageResponse> sendMessage(
            @PathVariable UUID channelId,
            @Valid @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(channelId, request);
    }

    @GetMapping(value = "/channels/{channelId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatMessageResponse>> streamMessages(@PathVariable UUID channelId) {
        return chatService.streamChannel(channelId);
    }

    @GetMapping("/contacts")
    public Flux<ChatContactResponse> getContacts() {
        return chatService.getContactsForCaller();
    }
}
