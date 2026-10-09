package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.UserStatusRequest;
import com.intellisure.customerpartyservice.dto.UserStatusResponse;
import com.intellisure.customerpartyservice.service.UserStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserStatusController {

    private final UserStatusService userStatusService;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @PutMapping("/{id}/status")
    public Mono<ResponseEntity<UserStatusResponse>> updateStatus(
            java.util.UUID id,
            @Valid @RequestBody UserStatusRequest request) {
        return userStatusService.updateStatus(id, request)
                .map(ResponseEntity::ok);
    }
}
