package com.intellisure.customerpartyservice.service;



import com.intellisure.customerpartyservice.controller.AuthController;
import com.intellisure.customerpartyservice.dto.RegisterRequest;
import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.DuplicateResourceException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepo userAccountRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);


    public Mono<UserResponse> register(RegisterRequest request) {
        log.info("Register request : {}", request.email());
        return userAccountRepo.findByEmail(request.email())
                .flatMap(existingUser->
                        Mono.<UserResponse>error(
                                new DuplicateResourceException(
                                        "User with email already exists"
                                )

                        )
                )
                .switchIfEmpty(
                        Mono.defer(()->{
                                    LocalDateTime now = LocalDateTime.now();
                                    String role = "VENDOR_APPLICANT".equalsIgnoreCase(request.registrationType())
                                            ? "VENDOR_APPLICANT" : "POLICYHOLDER";
                                    UserAccount userAccount = UserAccount.builder()
                                            .userId(UUID.randomUUID())
                                            .email(request.email())
                                            .passwordHash(passwordEncoder.encode(request.password()))
                                            .role(role)
                                            .accountStatus("ACTIVE")
                                            .displayName(request.displayName())
                                            .createdAt(now)
                                            .updatedAt(now)
                                            .build();
                                    return r2dbcEntityTemplate.insert(userAccount).map(userAccountMapper::toUserResponse);
                                })
                );
    }

    public Mono<UserResponse> getUserByEmail(String email) {
        return userAccountRepo.findByEmail(email)
                .map(userAccountMapper::toUserResponse);
    }

    public Mono<UserResponse> getUserById(UUID id) {
        return userAccountRepo.findById(id).map(userAccountMapper::toUserResponse)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException("User Not Found")
                        )
                );
    }

    public Flux<UserResponse> getUsersByRole(String role) {
        return findAvailableEmployees(role, "ACTIVE");
    }

    public Flux<UserResponse> findAvailableEmployees(String role, String status) {
        String normalizedStatus = (status == null || status.isBlank()) ? "ACTIVE" : status.trim().toUpperCase(Locale.ROOT);
        if (role == null || role.isBlank()) {
            return userAccountRepo.findByAccountStatus(normalizedStatus)
                    .filter(user -> !"POLICYHOLDER".equalsIgnoreCase(normalizeRole(user.getRole())))
                    .map(userAccountMapper::toUserResponse);
        }
        String normalizedRole = normalizeRole(role);
        return userAccountRepo.findByRoleAndAccountStatus(normalizedRole, normalizedStatus)
                .map(userAccountMapper::toUserResponse);
    }

    public Flux<UserResponse> getUsersForAdministration(String role, String status, String search) {
        String normalizedRole = role == null || role.isBlank() ? null : normalizeRole(role);
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase(Locale.ROOT);
        String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);

        return userAccountRepo.findAll()
                .filter(user -> normalizedRole == null || normalizedRole.equalsIgnoreCase(normalizeRole(user.getRole())))
                .filter(user -> normalizedStatus == null
                        || (user.getAccountStatus() != null
                        && normalizedStatus.equalsIgnoreCase(user.getAccountStatus())))
                .filter(user -> normalizedSearch.isBlank()
                        || contains(user.getEmail(), normalizedSearch)
                        || contains(user.getDisplayName(), normalizedSearch)
                        || contains(user.getRole(), normalizedSearch))
                .sort((left, right) -> {
                    LocalDateTime leftCreated = left.getCreatedAt();
                    LocalDateTime rightCreated = right.getCreatedAt();
                    if (leftCreated == null && rightCreated == null) return 0;
                    if (leftCreated == null) return 1;
                    if (rightCreated == null) return -1;
                    return rightCreated.compareTo(leftCreated);
                })
                .map(userAccountMapper::toUserResponse);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "POLICYHOLDER";
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        return normalized;
    }

}
