package com.intellisure.customerpartyservice.controller;

import com.intellisure.customerpartyservice.dto.RoleAssignmentRequest;
import com.intellisure.customerpartyservice.dto.RoleAssignmentResponse;
import com.intellisure.customerpartyservice.service.UserRoleService;
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
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PreAuthorize("hasAnyRole('SYSTEM_ADMINISTRATOR', 'ADMIN')")
    @PutMapping("/{id}/roles")
    public Mono<ResponseEntity<RoleAssignmentResponse>> assignRoles(
            java.util.UUID id,
            @Valid @RequestBody RoleAssignmentRequest request) {
        return userRoleService.assignRoles(id, request)
                .map(ResponseEntity::ok);
    }
}
