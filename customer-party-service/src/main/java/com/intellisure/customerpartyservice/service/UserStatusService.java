package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.UserStatusRequest;
import com.intellisure.customerpartyservice.dto.UserStatusResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.BusinessException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import com.intellisure.customerpartyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatusService {

    private final UserAccountRepo userAccountRepo;
    private final R2dbcEntityTemplate entityTemplate;
    private final SecurityActorService securityActorService;

    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "INACTIVE", "SUSPENDED");

    public Mono<UserStatusResponse> updateStatus(UUID userId, UserStatusRequest request) {
        String newStatus = normalizeStatus(request.status());

        if (!VALID_STATUSES.contains(newStatus)) {
            return Mono.error(new BusinessException(
                    "Invalid status: " + newStatus + ". Valid statuses: " + VALID_STATUSES));
        }

        return securityActorService.currentUserId()
                .flatMap(changedBy -> userAccountRepo.findById(userId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found: " + userId)))
                        .flatMap(user -> {
                            String oldStatus = user.getAccountStatus();
                            if (oldStatus.equalsIgnoreCase(newStatus)) {
                                return Mono.error(new BusinessException("User already has status: " + newStatus));
                            }

                            user.setAccountStatus(newStatus);
                            user.setUpdatedAt(LocalDateTime.now());

                            return entityTemplate.update(user)
                                    .flatMap(updated -> {
                                        LocalDateTime now = LocalDateTime.now();
                                        return Mono.just(new UserStatusResponse(
                                                userId,
                                                oldStatus,
                                                newStatus,
                                                changedBy.toString(),
                                                now.toString()
                                        ));
                                    });
                        }));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ACTIVE";
        }
        return status.trim().toUpperCase();
    }
}