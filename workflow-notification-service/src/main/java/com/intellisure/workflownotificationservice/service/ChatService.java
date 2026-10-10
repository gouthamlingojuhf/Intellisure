package com.intellisure.workflownotificationservice.service;

import com.intellisure.workflownotificationservice.dto.*;
import com.intellisure.workflownotificationservice.entity.*;
import com.intellisure.workflownotificationservice.exception.AccessDeniedBusinessException;
import com.intellisure.workflownotificationservice.repository.ChatChannelRepository;
import com.intellisure.workflownotificationservice.repository.ChatMessageRepository;
import com.intellisure.workflownotificationservice.repository.ChatParticipantRepository;
import com.intellisure.workflownotificationservice.security.SecurityActorService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatChannelRepository channelRepository;
    private final ChatParticipantRepository participantRepository;
    private final ChatMessageRepository messageRepository;
    private final SecurityActorService securityActorService;
    private final WebClient.Builder webClientBuilder;

    // In-memory bus for real-time SSE event publishing per channel
    private final Sinks.Many<ChatMessageResponse> messageSink = Sinks.many().multicast().directBestEffort();

    private static final List<String> EMPLOYEE_ROLES = List.of(
            "UNDERWRITER", "CLAIMS_ADJUSTER", "CLAIMS_MANAGER", "RISK_ENGINEER", "ADMIN", "SYSTEM_ADMINISTRATOR", "VENDOR_MANAGER"
    );

    @PostConstruct
    public void init() {
        // Ensure standard internal team channels exist
        ensureTeamChannel("#general-staff", "All Employees general discussion").subscribe();
        ensureTeamChannel("#underwriting-desk", "Underwriting risk & quote triage").subscribe();
        ensureTeamChannel("#claims-operations", "Claims adjusters and recovery desk").subscribe();
        ensureTeamChannel("#vendor-operations", "Vendor network management, dispatch & performance").subscribe();
    }

    private Mono<Void> ensureTeamChannel(String name, String description) {
        return channelRepository.findByChannelType(ChatChannelType.INTERNAL_GROUP)
                .filter(ch -> name.equalsIgnoreCase(ch.getName()))
                .hasElements()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) return Mono.empty();
                    ChatChannel ch = ChatChannel.builder()
                            .channelId(UUID.randomUUID())
                            .name(name)
                            .channelType(ChatChannelType.INTERNAL_GROUP)
                            .createdBy(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .isNew(true)
                            .build();
                    return channelRepository.save(ch).then();
                })
                .onErrorResume(err -> {
                    log.warn("Could not pre-seed channel {}: {}", name, err.getMessage());
                    return Mono.empty();
                });
    }

    public Flux<ChatChannelResponse> getChannelsForCaller() {
        return Mono.zip(
                securityActorService.currentUserId(),
                securityActorService.currentRole()
        ).flatMapMany(tuple -> {
            UUID userId = tuple.getT1();
            String role = tuple.getT2();
            boolean isEmployee = isEmployeeRole(role);

            Flux<ChatChannel> channelsFlux;
            if (isEmployee) {
                // Employees see all internal team channels PLUS their direct and customer support chats
                Flux<ChatChannel> groupChannels = channelRepository.findByChannelType(ChatChannelType.INTERNAL_GROUP);
                Flux<ChatChannel> myChannels = channelRepository.findChannelsByUserId(userId);
                channelsFlux = Flux.concat(groupChannels, myChannels)
                        .distinct(ChatChannel::getChannelId);
            } else {
                // Policyholders ONLY see channels where they are an active participant
                channelsFlux = channelRepository.findChannelsByUserId(userId);
            }

            return channelsFlux.flatMap(channel -> toChannelResponse(channel, userId));
        });
    }

    public Mono<ChatChannelResponse> createOrGetChannel(CreateChannelRequest request) {
        return Mono.zip(
                securityActorService.currentUserId(),
                securityActorService.currentRole(),
                securityActorService.currentDisplayName()
        ).flatMap(tuple -> {
            UUID callerId = tuple.getT1();
            String callerRole = tuple.getT2();
            String callerName = tuple.getT3();
            boolean isEmployee = isEmployeeRole(callerRole);

            // If entityType and entityId provided (e.g. quote or claim), check if channel already exists
            if (request.entityType() != null && request.entityId() != null) {
                return channelRepository.findByEntityTypeAndEntityId(request.entityType(), request.entityId())
                        .flatMap(existing -> toChannelResponse(existing, callerId))
                        .switchIfEmpty(Mono.defer(() -> createNewChannel(request, callerId, callerRole, callerName)));
            }

            // Direct 1-on-1 chat
            if (request.channelType() == ChatChannelType.DIRECT && request.participantUserIds() != null && !request.participantUserIds().isEmpty()) {
                UUID otherUserId = request.participantUserIds().get(0);
                // Check if direct channel already exists between these two users
                return findDirectChannelBetween(callerId, otherUserId)
                        .flatMap(existing -> toChannelResponse(existing, callerId))
                        .switchIfEmpty(Mono.defer(() -> createNewChannel(request, callerId, callerRole, callerName)));
            }

            // Group channel: internal only
            if (!isEmployee && request.channelType() == ChatChannelType.INTERNAL_GROUP) {
                return Mono.error(new AccessDeniedBusinessException("Policyholders cannot create internal group channels"));
            }

            return createNewChannel(request, callerId, callerRole, callerName);
        });
    }

    private Mono<ChatChannel> findDirectChannelBetween(UUID user1, UUID user2) {
        return participantRepository.findByUserId(user1)
                .flatMap(p1 -> participantRepository.findByUserId(user2)
                        .filter(p2 -> p1.getChannelId().equals(p2.getChannelId()))
                        .map(ChatParticipant::getChannelId))
                .flatMap(channelRepository::findById)
                .filter(ch -> ch.getChannelType() == ChatChannelType.DIRECT)
                .next();
    }

    private Mono<ChatChannelResponse> createNewChannel(
            CreateChannelRequest request, UUID callerId, String callerRole, String callerName) {
        UUID channelId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ChatChannel channel = ChatChannel.builder()
                .channelId(channelId)
                .name(request.name())
                .channelType(request.channelType())
                .entityType(request.entityType())
                .entityId(request.entityId())
                .createdBy(callerId)
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return channelRepository.save(channel)
                .flatMap(saved -> {
                    List<UUID> participantIds = new ArrayList<>();
                    participantIds.add(callerId);
                    if (request.participantUserIds() != null) {
                        for (UUID id : request.participantUserIds()) {
                            if (!participantIds.contains(id)) {
                                participantIds.add(id);
                            }
                        }
                    }

                    List<Mono<ChatParticipant>> participantSaves = participantIds.stream().map(pid -> {
                        ChatParticipant cp = ChatParticipant.builder()
                                .participantId(UUID.randomUUID())
                                .channelId(channelId)
                                .userId(pid)
                                .role(pid.equals(callerId) ? callerRole : "PARTICIPANT")
                                .displayName(pid.equals(callerId) ? callerName : "Participant")
                                .joinedAt(now)
                                .isNew(true)
                                .build();
                        return participantRepository.save(cp);
                    }).toList();

                    return Flux.concat(participantSaves).then(toChannelResponse(saved, callerId));
                });
    }

    public Mono<ChatMessageResponse> sendMessage(UUID channelId, SendMessageRequest request) {
        return Mono.zip(
                securityActorService.currentUserId(),
                securityActorService.currentRole(),
                securityActorService.currentDisplayName()
        ).flatMap(tuple -> {
            UUID senderId = tuple.getT1();
            String senderRole = tuple.getT2();
            String senderName = tuple.getT3();

            return channelRepository.findById(channelId)
                    .switchIfEmpty(Mono.error(new AccessDeniedBusinessException("Chat channel not found")))
                    .flatMap(channel -> assertAccessToChannel(channel, senderId, senderRole)
                            .then(Mono.defer(() -> {
                                LocalDateTime now = LocalDateTime.now();
                                ChatMessage message = ChatMessage.builder()
                                        .messageId(UUID.randomUUID())
                                        .channelId(channelId)
                                        .senderId(senderId)
                                        .senderName(senderName)
                                        .senderRole(senderRole)
                                        .content(request.content().trim())
                                        .createdAt(now)
                                        .isNew(true)
                                        .build();

                                channel.setUpdatedAt(now);
                                return channelRepository.save(channel)
                                        .then(messageRepository.save(message))
                                        .map(this::toMessageResponse)
                                        .doOnNext(msgResp -> messageSink.tryEmitNext(msgResp));
                            })));
        });
    }

    public Flux<ChatMessageResponse> getMessages(UUID channelId) {
        return Mono.zip(
                securityActorService.currentUserId(),
                securityActorService.currentRole()
        ).flatMapMany(tuple -> {
            UUID userId = tuple.getT1();
            String role = tuple.getT2();

            return channelRepository.findById(channelId)
                    .switchIfEmpty(Mono.error(new AccessDeniedBusinessException("Chat channel not found")))
                    .flatMapMany(channel -> assertAccessToChannel(channel, userId, role)
                            .thenMany(messageRepository.findByChannelIdOrderByCreatedAtAsc(channelId)
                                    .map(this::toMessageResponse)));
        });
    }

    public Flux<ServerSentEvent<ChatMessageResponse>> streamChannel(UUID channelId) {
        return messageSink.asFlux()
                .filter(msg -> channelId.equals(msg.channelId()))
                .map(msg -> ServerSentEvent.<ChatMessageResponse>builder()
                        .id(msg.messageId().toString())
                        .event("message")
                        .data(msg)
                        .build());
    }

    public Flux<ChatContactResponse> getContactsForCaller() {
        return Mono.zip(
                securityActorService.currentUserId(),
                securityActorService.currentRole(),
                securityActorService.currentCustomerId().defaultIfEmpty(UUID.fromString("00000000-0000-0000-0000-000000000000"))
        ).flatMapMany(tuple -> {
            UUID userId = tuple.getT1();
            String role = tuple.getT2();
            UUID customerId = tuple.getT3();
            boolean isEmployee = isEmployeeRole(role);
            boolean isVendor = isVendorRole(role);

            if (isEmployee) {
                // Employees see available internal colleagues
                Flux<ChatContactResponse> colleagues = fetchAvailableEmployees()
                        .filter(emp -> !emp.userId().equals(userId))
                        .map(emp -> new ChatContactResponse(
                                emp.userId(),
                                emp.displayName() != null ? emp.displayName() : emp.email(),
                                emp.email(),
                                emp.role(),
                                "Internal Staff",
                                null,
                                null
                        ));

                if (role.toUpperCase().contains("VENDOR_MANAGER") || role.toUpperCase().contains("ADMIN")) {
                    Flux<ChatContactResponse> vendors = fetchActiveVendors();
                    return Flux.concat(colleagues, vendors);
                }
                return colleagues;
            } else if (isVendor) {
                // Vendors can chat with Vendor Managers and assigned Policyholders
                return fetchContactsForVendor(userId);
            } else {
                // Policyholder sees their assigned underwriters, claims adjusters, and assigned service vendors
                return fetchAssignedStaffForPolicyholder(customerId);
            }
        });
    }

    private Flux<ChatContactResponse> fetchAvailableEmployees() {
        return webClientBuilder.build()
                .get()
                .uri("http://customer-party-service/api/users/available?status=ACTIVE")
                .retrieve()
                .bodyToFlux(UserDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch available employees from customer-party-service: {}", err.getMessage());
                    return Flux.empty();
                })
                .map(u -> new ChatContactResponse(
                        u.userId(),
                        u.displayName() != null ? u.displayName() : u.email(),
                        u.email(),
                        u.role(),
                        "Internal Operations",
                        null,
                        null
                ));
    }

    private Flux<ChatContactResponse> fetchActiveVendors() {
        return webClientBuilder.build()
                .get()
                .uri("http://vendor-partner-service/api/vendors")
                .retrieve()
                .bodyToMono(VendorListDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch vendors from vendor-partner-service: {}", err.getMessage());
                    return Mono.empty();
                })
                .flatMapMany(resp -> Flux.fromIterable(resp.items() != null ? resp.items() : List.<VendorItemDto>of()))
                .map(v -> new ChatContactResponse(
                        v.vendorId(),
                        v.displayName() != null ? v.displayName() : v.legalName(),
                        v.contactEmail(),
                        "VENDOR",
                        "Network Partner (" + (v.vendorType() != null ? v.vendorType() : "Service Provider") + ")",
                        null,
                        null
                ));
    }

    private Flux<ChatContactResponse> fetchContactsForVendor(UUID vendorUserId) {
        // 1. Vendor Manager contacts
        Flux<ChatContactResponse> vendorManagers = webClientBuilder.build()
                .get()
                .uri("http://customer-party-service/api/users/role/VENDOR_MANAGER")
                .retrieve()
                .bodyToFlux(UserDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch vendor managers: {}", err.getMessage());
                    return Flux.empty();
                })
                .map(u -> new ChatContactResponse(
                        u.userId(),
                        u.displayName() != null ? u.displayName() : "Vendor Operations Manager",
                        u.email(),
                        "VENDOR_MANAGER",
                        "Vendor Operations & Performance Desk",
                        null,
                        null
                ));

        // 2. Query vendor assignments for this vendor to connect with assigned policyholders
        Flux<ChatContactResponse> policyholderContacts = webClientBuilder.build()
                .get()
                .uri("http://vendor-partner-service/api/vendor-assignments")
                .retrieve()
                .bodyToMono(VendorAssignmentListDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch vendor assignments for vendor: {}", err.getMessage());
                    return Mono.empty();
                })
                .flatMapMany(resp -> Flux.fromIterable(resp.items() != null ? resp.items() : List.<VendorAssignmentItemDto>of()))
                .filter(a -> a.claimId() != null)
                .flatMap(a -> webClientBuilder.build()
                        .get()
                        .uri("http://claims-service/api/claims/" + a.claimId())
                        .retrieve()
                        .bodyToMono(ClaimDetailDto.class)
                        .onErrorResume(err -> Mono.empty())
                        .flatMap(c -> {
                            if (c.customerId() == null) return Mono.empty();
                            return webClientBuilder.build()
                                    .get()
                                    .uri("http://customer-party-service/api/customers/" + c.customerId())
                                    .retrieve()
                                    .bodyToMono(CustomerDetailDto.class)
                                    .onErrorResume(err -> Mono.empty())
                                    .map(cust -> new ChatContactResponse(
                                            cust.userId() != null ? cust.userId() : c.customerId(),
                                            cust.businessName() != null ? cust.businessName() : "Policyholder (Claim " + c.claimNumber() + ")",
                                            cust.phone(),
                                            "POLICYHOLDER",
                                            "CLAIM",
                                            c.claimId(),
                                            "Assigned Work Order for Claim " + c.claimNumber()
                                    ));
                        }))
                .distinct(ChatContactResponse::userId);

        return Flux.concat(vendorManagers, policyholderContacts);
    }

    private Flux<ChatContactResponse> fetchAssignedStaffForPolicyholder(UUID customerId) {
        if (customerId.equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
            return Flux.empty();
        }

        // 1. Fetch assigned Underwriters from quote-policy-service
        Flux<ChatContactResponse> underwriterContacts = webClientBuilder.build()
                .get()
                .uri("http://quote-policy-service/api/quotes/customer/" + customerId)
                .retrieve()
                .bodyToFlux(QuoteDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch customer quotes for chat: {}", err.getMessage());
                    return Flux.empty();
                })
                .filter(q -> q.assignedUnderwriterId() != null)
                .map(q -> new ChatContactResponse(
                        q.assignedUnderwriterId(),
                        "Underwriter (Assigned)",
                        null,
                        "UNDERWRITER",
                        "QUOTE",
                        q.quoteId(),
                        "Quote Triage Desk (" + (q.quoteNumber() != null ? q.quoteNumber() : "Active Quote") + ")"
                ));

        // 2. Fetch assigned Adjusters from claims-service
        Flux<ClaimDetailDto> customerClaims = webClientBuilder.build()
                .get()
                .uri("http://claims-service/api/claims")
                .retrieve()
                .bodyToFlux(ClaimDetailDto.class)
                .onErrorResume(err -> {
                    log.warn("Could not fetch customer claims for chat: {}", err.getMessage());
                    return Flux.empty();
                })
                .filter(c -> customerId.equals(c.customerId()));

        Flux<ChatContactResponse> adjusterContacts = customerClaims
                .filter(c -> c.assignedAdjusterId() != null)
                .map(c -> new ChatContactResponse(
                        c.assignedAdjusterId(),
                        "Claims Adjuster (Assigned)",
                        null,
                        "CLAIMS_ADJUSTER",
                        "CLAIM",
                        c.claimId(),
                        "Claims Examination (" + (c.claimNumber() != null ? c.claimNumber() : "Active Claim") + ")"
                ));

        // 3. Fetch assigned Service Vendors on policyholder's claims
        Flux<ChatContactResponse> vendorContacts = customerClaims
                .flatMap(c -> webClientBuilder.build()
                        .get()
                        .uri("http://vendor-partner-service/api/vendor-assignments?claimId=" + c.claimId())
                        .retrieve()
                        .bodyToMono(VendorAssignmentListDto.class)
                        .onErrorResume(err -> Mono.empty())
                        .flatMapMany(resp -> Flux.fromIterable(resp.items() != null ? resp.items() : List.<VendorAssignmentItemDto>of()))
                        .flatMap(a -> webClientBuilder.build()
                                .get()
                                .uri("http://vendor-partner-service/api/vendors/" + a.vendorId())
                                .retrieve()
                                .bodyToMono(VendorItemDto.class)
                                .onErrorResume(err -> Mono.empty())
                                .map(v -> new ChatContactResponse(
                                        v.vendorId(),
                                        v.displayName() != null ? v.displayName() : v.legalName(),
                                        v.contactEmail(),
                                        "VENDOR",
                                        "CLAIM",
                                        c.claimId(),
                                        "Assigned Service Partner for Claim " + c.claimNumber()
                                ))));

        return Flux.concat(underwriterContacts, adjusterContacts, vendorContacts)
                .distinct(ChatContactResponse::userId);
    }

    private Mono<Void> assertAccessToChannel(ChatChannel channel, UUID userId, String role) {
        boolean isEmployee = isEmployeeRole(role);
        if (channel.getChannelType() == ChatChannelType.INTERNAL_GROUP) {
            if (isEmployee) return Mono.empty();
            return Mono.error(new AccessDeniedBusinessException("External users do not have access to internal channels"));
        }

        return participantRepository.existsByChannelIdAndUserId(channel.getChannelId(), userId)
                .flatMap(isParticipant -> {
                    if (Boolean.TRUE.equals(isParticipant)) return Mono.empty();
                    // If employee accessing a support channel, allow and join
                    if (isEmployee && channel.getChannelType() == ChatChannelType.CUSTOMER_SUPPORT) {
                        ChatParticipant p = ChatParticipant.builder()
                                .participantId(UUID.randomUUID())
                                .channelId(channel.getChannelId())
                                .userId(userId)
                                .role(role)
                                .displayName("Staff")
                                .joinedAt(LocalDateTime.now())
                                .isNew(true)
                                .build();
                        return participantRepository.save(p).then();
                    }
                    return Mono.error(new AccessDeniedBusinessException("You are not a participant in this conversation"));
                });
    }

    private Mono<ChatChannelResponse> toChannelResponse(ChatChannel channel, UUID currentUserId) {
        return participantRepository.findByChannelId(channel.getChannelId())
                .map(p -> new ChatParticipantResponse(
                        p.getParticipantId(),
                        p.getUserId(),
                        p.getRole(),
                        p.getDisplayName(),
                        p.getJoinedAt()
                ))
                .collectList()
                .flatMap(participants -> messageRepository.findByChannelIdOrderByCreatedAtAsc(channel.getChannelId())
                        .takeLast(1)
                        .map(this::toMessageResponse)
                        .next()
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty())
                        .map(optLastMsg -> new ChatChannelResponse(
                                channel.getChannelId(),
                                channel.getName(),
                                channel.getChannelType(),
                                channel.getEntityType(),
                                channel.getEntityId(),
                                channel.getCreatedBy(),
                                channel.getCreatedAt(),
                                channel.getUpdatedAt(),
                                participants,
                                optLastMsg.orElse(null),
                                0
                        )));
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        return new ChatMessageResponse(
                msg.getMessageId(),
                msg.getChannelId(),
                msg.getSenderId(),
                msg.getSenderName(),
                msg.getSenderRole(),
                msg.getContent(),
                msg.getCreatedAt()
        );
    }

    private boolean isEmployeeRole(String role) {
        if (role == null) return false;
        String normalized = role.toUpperCase().replaceFirst("^ROLE_", "");
        return EMPLOYEE_ROLES.contains(normalized);
    }

    private boolean isVendorRole(String role) {
        if (role == null) return false;
        String normalized = role.toUpperCase().replaceFirst("^ROLE_", "");
        return normalized.equals("VENDOR") || normalized.equals("VENDOR_APPLICANT") || normalized.equals("VENDOR_PARTNER");
    }

    // Client DTO records for reactive inter-service queries
    private record UserDto(UUID userId, String email, String role, String displayName) {}
    private record QuoteDto(UUID quoteId, String quoteNumber, UUID assignedUnderwriterId) {}
    private record ClaimDetailDto(UUID claimId, String claimNumber, UUID customerId, UUID policyId, UUID assignedAdjusterId) {}
    private record CustomerDetailDto(UUID customerId, UUID userId, String businessName, String phone) {}
    private record VendorItemDto(UUID vendorId, String legalName, String displayName, String vendorType, String contactEmail) {}
    private record VendorListDto(List<VendorItemDto> items) {}
    private record VendorAssignmentItemDto(UUID assignmentId, UUID vendorId, UUID claimId, String status) {}
    private record VendorAssignmentListDto(List<VendorAssignmentItemDto> items) {}
}
