package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;

    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return userAccountService.getUserById(userId);
    }

    @GetMapping("/available")
    public Flux<UserResponse> getAvailableEmployees(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "ACTIVE") String status) {
        return userAccountService.findAvailableEmployees(role, status);
    }

    @GetMapping("/role/CLAIMS_ADJUSTER/available")
    public Flux<UserResponse> getAvailableClaimsAdjusters() {
        return userAccountService.findAvailableEmployees("CLAIMS_ADJUSTER", "ACTIVE");
    }

    @GetMapping("/role/{role}/available")
    public Flux<UserResponse> getAvailableByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "ACTIVE") String status) {
        return userAccountService.findAvailableEmployees(role, status);
    }

    @GetMapping("/role/{role}")
    public Flux<UserResponse> getUsersByRole(@PathVariable String role) {
        return userAccountService.getUsersByRole(role);
    }
}

