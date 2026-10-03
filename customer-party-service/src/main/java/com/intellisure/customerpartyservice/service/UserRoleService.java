package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.RoleAssignmentRequest;
import com.intellisure.customerpartyservice.dto.RoleAssignmentResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.BusinessException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import com.intellisure.customerpartyservice.security.SecurityActorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private final UserAccountRepo userAccountRepo;
    private final R2dbcEntityTemplate entityTemplate;
    private final SecurityActorService securityActorService;

    private static final Set<String> VALID_ROLES = Set.of(
            "POLICYHOLDER",
            "UNDERWRITER",
            "RISK_ENGINEER",
            "CLAIMS_ADJUSTER",
            "CLAIMS_MANAGER",
            "VENDOR_MANAGER",
            "SYSTEM_ADMINISTRATOR"
    );

    public Mono<RoleAssignmentResponse> assignRoles(UUID userId, RoleAssignmentRequest request) {
        String newRole = normalizeRole(request.role());

        if (!VALID_ROLES.contains(newRole)) {
            return Mono.error(new BusinessException(
                    "Invalid role: " + newRole + ". Valid roles: " + VALID_ROLES));
        }

        return securityActorService.currentUserId()
                .flatMap(changedBy -> userAccountRepo.findById(userId)
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found: " + userId)))
                        .flatMap(user -> {
                            String oldRole = user.getRole();
                            if (oldRole.equals(newRole)) {
                                return Mono.error(new BusinessException("User already has role: " + newRole));
                            }

                            user.setRole(newRole);
                            user.setUpdatedAt(LocalDateTime.now());

                            return entityTemplate.update(user)
                                    .flatMap(updated -> recordRoleAudit(userId, oldRole, newRole, changedBy));
                        }));
    }

    private Mono<RoleAssignmentResponse> recordRoleAudit(UUID userId, String oldRole, String newRole, UUID changedBy) {
        return securityActorService.currentUserId()
                .flatMap(actualChangedBy -> {
                    LocalDateTime now = LocalDateTime.now();
                    // For simplicity, return response directly
                    // In a full implementation, you'd insert into user_role_audit table
                    return Mono.just(new RoleAssignmentResponse(
                            userId,
                            Set.of(oldRole),
                            Set.of(newRole),
                            actualChangedBy.toString(),
                            now.toString()
                    ));
                });
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "POLICYHOLDER";
        }
        String normalized = role.trim().toUpperCase();
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }
}