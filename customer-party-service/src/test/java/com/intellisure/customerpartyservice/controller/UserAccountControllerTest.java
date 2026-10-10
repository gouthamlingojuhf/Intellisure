package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.service.UserAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;

class UserAccountControllerTest {

    @Mock
    private UserAccountService userAccountService;

    private UserAccountController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserAccountController(userAccountService);
    }

    @Test
    void getCurrentUserReturnsMappedResponse() {
        UUID userId = UUID.randomUUID();

        Jwt jwt = Jwt.withTokenValue("t")
            .header("alg", "none")
            .subject(userId.toString())
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(60))
            .build();

        UserResponse resp = new UserResponse(userId, "a@b.com", "POLICYHOLDER", "ACTIVE", "Owner", null, null);

        when(userAccountService.getUserById(userId)).thenReturn(Mono.just(resp));

        StepVerifier.create(controller.getCurrentUser(jwt))
                .expectNext(resp)
                .verifyComplete();
    }

    @Test
    void getAvailableClaimsAdjustersDelegatesToService() {
        UUID adjusterId = UUID.randomUUID();
        UserResponse resp = new UserResponse(adjusterId, "adj@intellisure.com", "CLAIMS_ADJUSTER", "ACTIVE", "Adjuster One", null, null);

        when(userAccountService.findAvailableEmployees("CLAIMS_ADJUSTER", "ACTIVE")).thenReturn(reactor.core.publisher.Flux.just(resp));

        StepVerifier.create(controller.getAvailableClaimsAdjusters())
                .expectNext(resp)
                .verifyComplete();
    }
}
