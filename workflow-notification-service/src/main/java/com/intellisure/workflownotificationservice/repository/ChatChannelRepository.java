package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.ChatChannel;
import com.intellisure.workflownotificationservice.entity.ChatChannelType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChatChannelRepository extends R2dbcRepository<ChatChannel, UUID> {

    Flux<ChatChannel> findByChannelType(ChatChannelType channelType);

    Mono<ChatChannel> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    @Query("SELECT c.* FROM chat_channel c INNER JOIN chat_participant p ON c.channel_id = p.channel_id WHERE p.user_id = :userId ORDER BY c.updated_at DESC")
    Flux<ChatChannel> findChannelsByUserId(UUID userId);
}
