package com.intellisure.customerpartyservice.controller;


import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.dto.LoginResponse;
import com.intellisure.customerpartyservice.dto.RegisterRequest;
import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.service.AuthService;
import com.intellisure.customerpartyservice.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserAccountService userAccountService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        log.info("Register request : {}", registerRequest);
        return userAccountService.register(registerRequest);
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }
}
