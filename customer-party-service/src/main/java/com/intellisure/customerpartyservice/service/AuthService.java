package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.dto.LoginResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.InvalidCredentialsException;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserAccountRepo userAccountRepo;

    private final BusinessCustomerRepository
            businessCustomerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private final R2dbcEntityTemplate entityTemplate;

    public Mono<LoginResponse> login(
            LoginRequest request
    ) {
        String normalizedEmail =
                request.email()
                        .trim()
                        .toLowerCase();

        return userAccountRepo
                .findByEmail(normalizedEmail)
                .switchIfEmpty(
                        Mono.error(
                                invalidCredentials()
                        )
                )
                .flatMap(user ->
                        authenticate(
                                user,
                                request.password()
                        )
                );
    }

    private Mono<LoginResponse> authenticate(
            UserAccount user,
            String password
    ) {
        if (!passwordEncoder.matches(
                password,
                user.getPasswordHash()
        )) {
            return Mono.error(
                    invalidCredentials()
            );
        }

        if (!"ACTIVE".equalsIgnoreCase(
                user.getAccountStatus()
        )) {
            return Mono.error(
                    invalidCredentials()
            );
        }

        log.info(
                "Credentials validated for userId={}, role={}",
                user.getUserId(),
                user.getRole()
        );

        if (isPolicyholder(user)) {
            return businessCustomerRepository
                    .findByUserId(user.getUserId())
                    .flatMap(customer ->
                            issueToken(
                                    user,
                                    customer.getCustomerId()
                            )
                    )
                    .switchIfEmpty(
                            Mono.defer(() ->
                                    issueToken(
                                            user,
                                            null
                                    )
                            )
                    );
        }


        return issueToken(
                user,
                null
        );
    }

    private Mono<LoginResponse> issueToken(
            UserAccount user,
            UUID customerId
    ) {
        String token =
                jwtService.generateToken(
                        user.getUserId(),
                        user.getRole(),
                        customerId
                );

        LocalDateTime now =
                LocalDateTime.now();

        user.setLastLoginAt(now);
        user.setUpdatedAt(now);

        return entityTemplate
                .update(user)
                .map(updatedUser ->
                        new LoginResponse(
                                token,
                                "Bearer",
                                jwtService
                                        .getExpirationSeconds(),
                                updatedUser.getUserId(),
                                customerId,
                                normalizeRole(
                                        updatedUser.getRole()
                                )
                        )
                );
    }

    private boolean isPolicyholder(
            UserAccount user
    ) {
        return "POLICYHOLDER".equals(
                normalizeRole(
                        user.getRole()
                )
        );
    }

    private String normalizeRole(
            String role
    ) {
        if (role == null
                || role.isBlank()) {
            return "";
        }

        String normalized =
                role.trim().toUpperCase();

        if (normalized.startsWith("ROLE_")) {
            return normalized.substring(5);
        }

        return normalized;
    }

    private InvalidCredentialsException
    invalidCredentials() {
        return new InvalidCredentialsException(
                "Invalid email or password"
        );
    }
}