package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.ChatMessage;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ChatMessageRepository extends R2dbcRepository<ChatMessage, UUID> {

    Flux<ChatMessage> findByChannelIdOrderByCreatedAtAsc(UUID channelId);
}
