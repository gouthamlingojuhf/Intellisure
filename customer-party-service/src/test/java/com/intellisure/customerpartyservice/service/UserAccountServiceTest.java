package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.RegisterRequest;
import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.DuplicateResourceException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepo userAccountRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserAccountMapper userAccountMapper;

    @Mock
    private R2dbcEntityTemplate entityTemplate;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService =
                new UserAccountService(
                        userAccountRepo,
                        passwordEncoder,
                        userAccountMapper,
                        entityTemplate
                );
    }

    @Test
    void shouldRegisterPolicyholder() {
        RegisterRequest request =
                new RegisterRequest(
                        "newowner@example.com",
                        "Password@123",
                        "New Owner"
                );

        when(userAccountRepo.findByEmail(
                request.email()
        )).thenReturn(Mono.empty());

        when(passwordEncoder.encode(
                request.password()
        )).thenReturn("bcrypt-hash");

        when(entityTemplate.insert(
                any(UserAccount.class)
        )).thenAnswer(invocation ->
                Mono.just(invocation.getArgument(0))
        );

        when(userAccountMapper.toUserResponse(
                any(UserAccount.class)
        )).thenAnswer(invocation -> {
            UserAccount user =
                    invocation.getArgument(0);

            return responseFrom(user);
        });

        StepVerifier.create(
                        userAccountService.register(request)
                )
                .assertNext(response -> {
                    assertNotNull(response.userId());

                    assertEquals(
                            request.email(),
                            response.email()
                    );

                    assertEquals(
                            "POLICYHOLDER",
                            response.role()
                    );

                    assertEquals(
                            "ACTIVE",
                            response.accountStatus()
                    );

                    assertEquals(
                            request.displayName(),
                            response.displayName()
                    );
                })
                .verifyComplete();

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(
                        UserAccount.class
                );

        verify(entityTemplate).insert(
                captor.capture()
        );

        UserAccount inserted =
                captor.getValue();

        assertEquals(
                "bcrypt-hash",
                inserted.getPasswordHash()
        );

        assertNotEquals(
                request.password(),
                inserted.getPasswordHash()
        );

        assertEquals(
                "POLICYHOLDER",
                inserted.getRole()
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request =
                new RegisterRequest(
                        "existing@example.com",
                        "Password@123",
                        "Existing User"
                );

        UserAccount existing =
                UserAccount.builder()
                        .userId(UUID.randomUUID())
                        .email(request.email())
                        .build();

        when(userAccountRepo.findByEmail(
                request.email()
        )).thenReturn(Mono.just(existing));

        StepVerifier.create(
                        userAccountService.register(request)
                )
                .expectErrorSatisfies(error -> {
                    assertEquals(
                            DuplicateResourceException.class,
                            error.getClass()
                    );

                    assertEquals(
                            "User with email already exists",
                            error.getMessage()
                    );
                })
                .verify();

        verify(passwordEncoder, never())
                .encode(any());

        verify(entityTemplate, never())
                .insert(any(UserAccount.class));
    }

    @Test
    void shouldFindUserById() {
        UserAccount user =
                UserAccount.builder()
                        .userId(UUID.randomUUID())
                        .email("owner@example.com")
                        .role("POLICYHOLDER")
                        .accountStatus("ACTIVE")
                        .displayName("Owner")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

        UserResponse response =
                responseFrom(user);

        when(userAccountRepo.findById(
                user.getUserId()
        )).thenReturn(Mono.just(user));

        when(userAccountMapper.toUserResponse(user))
                .thenReturn(response);

        StepVerifier.create(
                        userAccountService.getUserById(
                                user.getUserId()
                        )
                )
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    void shouldReturnNotFoundForUnknownUserId() {
        UUID userId = UUID.randomUUID();

        when(userAccountRepo.findById(userId))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        userAccountService.getUserById(
                                userId
                        )
                )
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    private UserResponse responseFrom(
            UserAccount user
    ) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getRole(),
                user.getAccountStatus(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}