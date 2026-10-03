package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.CustomerResponse;
import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.service.CustomerProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerProfileController {

    private final CustomerProfileService customerProfileService;

    @GetMapping("/me")
    public Mono<CustomerResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return customerProfileService.getCustomerProfileByUserId(userId);
    }

    @PutMapping("/me")
    public Mono<CustomerResponse> updateMyProfile(@AuthenticationPrincipal Jwt jwt, 
                                                  @Valid @RequestBody UpdateCustomerProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return customerProfileService.updateCustomerProfile(userId, request);
    }
}
