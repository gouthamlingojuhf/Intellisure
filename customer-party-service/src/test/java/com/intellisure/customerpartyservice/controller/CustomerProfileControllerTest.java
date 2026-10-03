package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.CustomerResponse;
import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.service.CustomerProfileService;
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

class CustomerProfileControllerTest {

    @Mock
    private CustomerProfileService service;

    private CustomerProfileController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new CustomerProfileController(service);
    }

    @Test
    void getMyProfileDelegatesToService() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("t").header("alg","none").subject(userId.toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        CustomerResponse resp = new CustomerResponse(UUID.randomUUID(), userId, null, null, null, null, null, null, null, null, null, null, null);

        when(service.getCustomerProfileByUserId(userId)).thenReturn(Mono.just(resp));

        StepVerifier.create(controller.getMyProfile(jwt))
                .expectNext(resp)
                .verifyComplete();
    }

    @Test
    void updateMyProfileDelegatesToService() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("t").header("alg","none").subject(userId.toString()).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        UpdateCustomerProfileRequest req = new UpdateCustomerProfileRequest("A","B",null,null,null,null,null,null,null);
        CustomerResponse resp = new CustomerResponse(UUID.randomUUID(), userId, null, null, null, null, null, null, null, null, null, null, null);

        when(service.updateCustomerProfile(userId, req)).thenReturn(Mono.just(resp));

        StepVerifier.create(controller.updateMyProfile(jwt, req))
                .expectNext(resp)
                .verifyComplete();
    }
}
