package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.AdminUserRequest;
import com.intellisure.customerpartyservice.dto.AdminUserResponse;
import com.intellisure.customerpartyservice.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final com.intellisure.customerpartyservice.service.UserAccountService userAccountService;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @GetMapping
    public Flux<com.intellisure.customerpartyservice.dto.UserResponse> listUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return userAccountService.getUsersForAdministration(role, status, search);
    }

    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @PostMapping
    public Mono<ResponseEntity<AdminUserResponse>> createAdminUser(
            @Valid @RequestBody AdminUserRequest request) {
        return adminUserService.registerAdminUser(request)
                .map(ResponseEntity::ok);
    }
}
