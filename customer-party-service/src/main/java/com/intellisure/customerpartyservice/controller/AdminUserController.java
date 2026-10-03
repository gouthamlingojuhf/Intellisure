package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.AdminUserRequest;
import com.intellisure.customerpartyservice.dto.AdminUserResponse;
import com.intellisure.customerpartyservice.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasRole('SYSTEM_ADMINISTRATOR')")
    @PostMapping
    public Mono<ResponseEntity<AdminUserResponse>> createAdminUser(
            @Valid @RequestBody AdminUserRequest request) {
        return adminUserService.registerAdminUser(request)
                .map(ResponseEntity::ok);
    }
}