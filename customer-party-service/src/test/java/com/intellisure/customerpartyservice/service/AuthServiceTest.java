package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.InvalidCredentialsException;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepo userAccountRepo;

    @Mock
    private BusinessCustomerRepository businessCustomerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userAccountRepo,
                businessCustomerRepository,
                passwordEncoder,
                jwtService,
                entityTemplate
        );
    }

    @Test
    void shouldLoginPolicyholderWithoutBusinessProfile() {
        UserAccount user = policyholder();

        LoginRequest request =
                new LoginRequest(
                        user.getEmail(),
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(user.getEmail()))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(businessCustomerRepository.findByUserId(
                user.getUserId()
        )).thenReturn(Mono.empty());

        when(jwtService.generateToken(
                user.getUserId(),
                user.getRole(),
                null
        )).thenReturn("policyholder-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(2592000L);

        when(entityTemplate.update(any(UserAccount.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(
                        authService.login(request)
                )
                .assertNext(response -> {
                    assertEquals(
                            "policyholder-token",
                            response.accessToken()
                    );

                    assertEquals(
                            "Bearer",
                            response.tokenType()
                    );

                    assertEquals(
                            2592000L,
                            response.expiresIn()
                    );

                    assertEquals(
                            user.getUserId(),
                            response.userId()
                    );

                    assertNull(response.customerId());

                    assertEquals(
                            "POLICYHOLDER",
                            response.role()
                    );

                    assertNotNull(user.getLastLoginAt());
                })
                .verifyComplete();

        verify(jwtService).generateToken(
                user.getUserId(),
                "POLICYHOLDER",
                null
        );

        verify(entityTemplate).update(user);
    }

    @Test
    void shouldLoginPolicyholderWithBusinessProfile() {
        UserAccount user = policyholder();
        UUID customerId = UUID.randomUUID();

        BusinessCustomer customer =
                BusinessCustomer.builder()
                        .customerId(customerId)
                        .userId(user.getUserId())
                        .businessName("Goutham Restaurant")
                        .ownerName("Goutham")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        LoginRequest request =
                new LoginRequest(
                        user.getEmail(),
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(user.getEmail()))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(businessCustomerRepository.findByUserId(
                user.getUserId()
        )).thenReturn(Mono.just(customer));

        when(jwtService.generateToken(
                user.getUserId(),
                user.getRole(),
                customerId
        )).thenReturn("policyholder-token-with-customer");

        when(jwtService.getExpirationSeconds())
                .thenReturn(2592000L);

        when(entityTemplate.update(any(UserAccount.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(
                        authService.login(request)
                )
                .assertNext(response -> {
                    assertEquals(
                            "policyholder-token-with-customer",
                            response.accessToken()
                    );

                    assertEquals(
                            customerId,
                            response.customerId()
                    );

                    assertEquals(
                            "POLICYHOLDER",
                            response.role()
                    );
                })
                .verifyComplete();

        verify(jwtService).generateToken(
                user.getUserId(),
                "POLICYHOLDER",
                customerId
        );
    }

    @Test
    void shouldLoginInternalUserWithoutBusinessProfile() {
        UserAccount underwriter =
                UserAccount.builder()
                        .userId(UUID.randomUUID())
                        .email("underwriter@intellisure.com")
                        .passwordHash("encoded-password")
                        .role("UNDERWRITER")
                        .accountStatus("ACTIVE")
                        .displayName("Underwriter")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        LoginRequest request =
                new LoginRequest(
                        underwriter.getEmail(),
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(
                underwriter.getEmail()
        )).thenReturn(Mono.just(underwriter));

        when(passwordEncoder.matches(
                request.password(),
                underwriter.getPasswordHash()
        )).thenReturn(true);

        when(jwtService.generateToken(
                underwriter.getUserId(),
                "UNDERWRITER",
                null
        )).thenReturn("underwriter-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(2592000L);

        when(entityTemplate.update(any(UserAccount.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(
                        authService.login(request)
                )
                .assertNext(response -> {
                    assertEquals(
                            "underwriter-token",
                            response.accessToken()
                    );

                    assertNull(response.customerId());

                    assertEquals(
                            "UNDERWRITER",
                            response.role()
                    );
                })
                .verifyComplete();

        verify(
                businessCustomerRepository,
                never()
        ).findByUserId(any(UUID.class));
    }

    @Test
    void shouldNormalizeBlankAndPrefixedInternalRoles() {
        for (String role : new String[]{null, " ", " ROLE_UNDERWRITER "}) {
            UserAccount user = UserAccount.builder()
                    .userId(UUID.randomUUID())
                    .email("internal-" + UUID.randomUUID() + "@intellisure.com")
                    .passwordHash("encoded-password")
                    .role(role)
                    .accountStatus("ACTIVE")
                    .displayName("Internal User")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            LoginRequest request = new LoginRequest(user.getEmail(), "Password@123");
            when(userAccountRepo.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
            when(passwordEncoder.matches(request.password(), user.getPasswordHash())).thenReturn(true);
            when(jwtService.generateToken(user.getUserId(), role, null)).thenReturn("internal-token");
            when(jwtService.getExpirationSeconds()).thenReturn(2592000L);
            when(entityTemplate.update(any(UserAccount.class)))
                    .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

            StepVerifier.create(authService.login(request))
                    .assertNext(response -> assertEquals("internal-token", response.accessToken()))
                    .verifyComplete();
        }
    }

    @Test
    void shouldRejectWrongPassword() {
        UserAccount user = policyholder();

        LoginRequest request =
                new LoginRequest(
                        user.getEmail(),
                        "WrongPassword"
                );

        when(userAccountRepo.findByEmail(user.getEmail()))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(false);

        StepVerifier.create(
                        authService.login(request)
                )
                .expectErrorSatisfies(error -> {
                    assertEquals(
                            InvalidCredentialsException.class,
                            error.getClass()
                    );

                    assertEquals(
                            "Invalid email or password",
                            error.getMessage()
                    );
                })
                .verify();

        verify(jwtService, never())
                .generateToken(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldRejectUnknownEmail() {
        LoginRequest request =
                new LoginRequest(
                        "unknown@example.com",
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(
                request.email()
        )).thenReturn(Mono.empty());

        StepVerifier.create(
                        authService.login(request)
                )
                .expectError(InvalidCredentialsException.class)
                .verify();
    }

    @Test
    void shouldRejectInactiveAccount() {
        UserAccount user = policyholder();
        user.setAccountStatus("DISABLED");

        LoginRequest request =
                new LoginRequest(
                        user.getEmail(),
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(user.getEmail()))
                .thenReturn(Mono.just(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        StepVerifier.create(
                        authService.login(request)
                )
                .expectError(InvalidCredentialsException.class)
                .verify();

        verify(jwtService, never())
                .generateToken(
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldNormalizeLoginEmail() {
        UserAccount user = policyholder();

        LoginRequest request =
                new LoginRequest(
                        "  OWNER@RESTAURANT.COM  ",
                        "Password@123"
                );

        when(userAccountRepo.findByEmail(
                "owner@restaurant.com"
        )).thenReturn(Mono.just(user));

        when(passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        )).thenReturn(true);

        when(businessCustomerRepository.findByUserId(
                user.getUserId()
        )).thenReturn(Mono.empty());

        when(jwtService.generateToken(
                user.getUserId(),
                user.getRole(),
                null
        )).thenReturn("token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(2592000L);

        when(entityTemplate.update(any(UserAccount.class)))
                .thenAnswer(invocation ->
                        Mono.just(invocation.getArgument(0))
                );

        StepVerifier.create(
                        authService.login(request)
                )
                .expectNextCount(1)
                .verifyComplete();

        verify(userAccountRepo)
                .findByEmail("owner@restaurant.com");
    }

    private UserAccount policyholder() {
        return UserAccount.builder()
                .userId(UUID.randomUUID())
                .email("owner@restaurant.com")
                .passwordHash("encoded-password")
                .role("POLICYHOLDER")
                .accountStatus("ACTIVE")
                .displayName("Goutham")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
