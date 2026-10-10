package com.intellisure.workflownotificationservice.repository;

import com.intellisure.workflownotificationservice.entity.ChatParticipant;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChatParticipantRepository extends R2dbcRepository<ChatParticipant, UUID> {

    Flux<ChatParticipant> findByChannelId(UUID channelId);

    Flux<ChatParticipant> findByUserId(UUID userId);

    Mono<ChatParticipant> findByChannelIdAndUserId(UUID channelId, UUID userId);

    Mono<Boolean> existsByChannelIdAndUserId(UUID channelId, UUID userId);
}
