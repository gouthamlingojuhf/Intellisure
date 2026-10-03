package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.dto.LoginResponse;
import com.intellisure.customerpartyservice.dto.RegisterRequest;
import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.service.AuthService;
import com.intellisure.customerpartyservice.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AuthController.class
            );

    private final AuthService authService;
    private final UserAccountService userAccountService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> register(
            @Valid @RequestBody
            RegisterRequest request
    ) {
        /*
         * Do not log the complete request if it contains
         * the plaintext password.
         */
        log.info(
                "Registration requested for email: {}",
                request.email()
        );

        return userAccountService.register(
                request
        );
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public Mono<UserResponse> me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return userAccountService
                .getUserById(
                        UUID.fromString(
                                jwt.getSubject()
                        )
                );
    }
}