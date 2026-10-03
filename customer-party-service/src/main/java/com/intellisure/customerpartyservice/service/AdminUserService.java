package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.AdminUserRequest;
import com.intellisure.customerpartyservice.dto.AdminUserResponse;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.BusinessException;
import com.intellisure.customerpartyservice.exception.DuplicateResourceException;
import com.intellisure.customerpartyservice.mapper.BusinessCustomerMapper;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserAccountRepo userAccountRepo;
    private final BusinessCustomerRepository businessCustomerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
    private final BusinessCustomerMapper businessCustomerMapper;
    private final R2dbcEntityTemplate entityTemplate;

    private static final Set<String> ADMIN_CREATABLE_ROLES = Set.of(
            "UNDERWRITER",
            "RISK_ENGINEER",
            "CLAIMS_ADJUSTER",
            "CLAIMS_MANAGER",
            "VENDOR_MANAGER",
            "SYSTEM_ADMINISTRATOR"
    );

    public Mono<AdminUserResponse> registerAdminUser(AdminUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String normalizedRole = normalizeRole(request.role());

        if (!ADMIN_CREATABLE_ROLES.contains(normalizedRole)) {
            return Mono.error(new BusinessException(
                    "Invalid role for admin creation: " + normalizedRole +
                    ". Allowed roles: " + ADMIN_CREATABLE_ROLES));
        }

        return userAccountRepo.findByEmail(normalizedEmail)
                .flatMap(existing -> Mono.<AdminUserResponse>error(
                        new DuplicateResourceException("User with email already exists: " + normalizedEmail)))
                .switchIfEmpty(Mono.defer(() -> createAdminUser(normalizedEmail, normalizedRole, request)));
    }

    private Mono<AdminUserResponse> createAdminUser(String email, String role, AdminUserRequest request) {
        LocalDateTime now = LocalDateTime.now();
        UUID userId = UUID.randomUUID();

        UserAccount userAccount = UserAccount.builder()
                .userId(userId)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .accountStatus("ACTIVE")
                .displayName(request.displayName())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return entityTemplate.insert(UserAccount.class)
                .using(userAccount)
                .flatMap(savedUser -> {
                    if ("POLICYHOLDER".equals(role)) {
                        return createBusinessCustomer(savedUser, request, now)
                                .map(customer -> new AdminUserResponse(
                                        savedUser.getUserId(),
                                        customer.getCustomerId(),
                                        savedUser.getEmail(),
                                        savedUser.getDisplayName(),
                                        savedUser.getRole(),
                                        savedUser.getAccountStatus()
                                ));
                    } else {
                        return Mono.just(new AdminUserResponse(
                                savedUser.getUserId(),
                                null,
                                savedUser.getEmail(),
                                savedUser.getDisplayName(),
                                savedUser.getRole(),
                                savedUser.getAccountStatus()
                        ));
                    }
                });
    }

    private Mono<BusinessCustomer> createBusinessCustomer(UserAccount user, AdminUserRequest request, LocalDateTime now) {
        BusinessCustomer customer = BusinessCustomer.builder()
                .customerId(UUID.randomUUID())
                .userId(user.getUserId())
                .businessName(request.businessName() != null ? request.businessName() : user.getDisplayName())
                .ownerName(request.ownerName() != null ? request.ownerName() : user.getDisplayName())
                .businessType(request.businessType())
                .phone(request.phone())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .country(request.country())
                .postalCode(request.postalCode())
                .version(0L)
                .materialChangePending(false)
                .createdAt(now)
                .updatedAt(now)
                .isNew(true)
                .build();

        return entityTemplate.insert(BusinessCustomer.class)
                .using(customer);
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "POLICYHOLDER";
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }
}