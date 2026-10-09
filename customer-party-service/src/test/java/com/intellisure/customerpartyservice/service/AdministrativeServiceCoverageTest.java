package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.*;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.BusinessException;
import com.intellisure.customerpartyservice.exception.DuplicateResourceException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.mapper.BusinessCustomerMapper;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import com.intellisure.customerpartyservice.security.SecurityActorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.core.ReactiveInsertOperation.ReactiveInsert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdministrativeServiceCoverageTest {
    private UserAccountRepo users;
    private BusinessCustomerRepository customers;
    private R2dbcEntityTemplate template;
    private SecurityActorService security;
    private final UUID actor = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        users = mock(UserAccountRepo.class);
        customers = mock(BusinessCustomerRepository.class);
        template = mock(R2dbcEntityTemplate.class);
        security = mock(SecurityActorService.class);
        lenient().when(security.currentUserId()).thenReturn(Mono.just(actor));
        lenient().when(template.update(any(UserAccount.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
    }

    private UserAccount user(String role, String status) {
        return UserAccount.builder().userId(UUID.randomUUID()).email("user@example.com")
                .passwordHash("hash").role(role).accountStatus(status).displayName("User")
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }

    private void stubClassInsert(Class<?> type) {
        ReactiveInsert insert = mock(ReactiveInsert.class);
        when(template.insert(type)).thenReturn(insert);
        when(insert.using(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
    }

    @Test
    void adminCreationValidatesRolesDuplicatesAndCreatesEmployees() {
        UserAccountMapper userMapper = mock(UserAccountMapper.class);
        BusinessCustomerMapper customerMapper = mock(BusinessCustomerMapper.class);
        AdminUserService service = new AdminUserService(users, mock(org.springframework.security.crypto.password.PasswordEncoder.class),
                userMapper, template);
        AdminUserRequest request = new AdminUserRequest(" Admin@Example.com ", "Password123", "Admin", "ROLE_UNDERWRITER",
                null, null, null, "Retail", null, null, null, "US", null);
        when(userMapper.toUserResponse(any())).thenReturn(mock(UserResponse.class));
        when(users.findByEmail("admin@example.com")).thenReturn(Mono.empty());
        when(template.insert(UserAccount.class)).thenReturn(mock(ReactiveInsert.class));
        ReactiveInsert accountInsert = template.insert(UserAccount.class);
        when(accountInsert.using(any())).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(mock(org.springframework.security.crypto.password.PasswordEncoder.class).encode(any())).thenReturn("hash");
        // Exercise with a real encoder mock so the service persists a normalized employee.
        org.springframework.security.crypto.password.PasswordEncoder encoder = mock(org.springframework.security.crypto.password.PasswordEncoder.class);
        service = new AdminUserService(users, encoder, userMapper, template);
        when(encoder.encode(any())).thenReturn("hash");
        StepVerifier.create(service.registerAdminUser(request)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.registerAdminUser(new AdminUserRequest("a@b.com", "Password123", "A", "NOPE", null, null, null, null, null, null, null, null, null)))
                .expectError(BusinessException.class).verify();
        StepVerifier.create(service.registerAdminUser(new AdminUserRequest("a@b.com", "Password123", "A", null, null, null, null, null, null, null, null, null, null)))
                .expectError(BusinessException.class).verify();
        StepVerifier.create(service.registerAdminUser(new AdminUserRequest("a@b.com", "Password123", "A", " ", null, null, null, null, null, null, null, null, null)))
                .expectError(BusinessException.class).verify();
        when(users.findByEmail("admin@example.com")).thenReturn(Mono.just(user("UNDERWRITER", "ACTIVE")));
        StepVerifier.create(service.registerAdminUser(request)).expectError(DuplicateResourceException.class).verify();
    }

    @Test
    void rolesAndStatusesCoverValidationOwnershipAndUpdates() {
        UserAccount current = user("POLICYHOLDER", "ACTIVE");
        when(users.findById(current.getUserId())).thenReturn(Mono.just(current));
        UserRoleService roles = new UserRoleService(users, template, security);
        StepVerifier.create(roles.assignRoles(current.getUserId(), new RoleAssignmentRequest("ROLE_UNDERWRITER")))
                .expectNextCount(1).verifyComplete();
        current.setRole("UNDERWRITER");
        StepVerifier.create(roles.assignRoles(current.getUserId(), new RoleAssignmentRequest("UNDERWRITER")))
                .expectError(BusinessException.class).verify();
        current.setRole("UNDERWRITER");
        StepVerifier.create(roles.assignRoles(current.getUserId(), new RoleAssignmentRequest(null)))
                .expectNextCount(1).verifyComplete();
        current.setRole("UNDERWRITER");
        StepVerifier.create(roles.assignRoles(current.getUserId(), new RoleAssignmentRequest(" ")))
                .expectNextCount(1).verifyComplete();
        StepVerifier.create(roles.assignRoles(current.getUserId(), new RoleAssignmentRequest("INVALID")))
                .expectError(BusinessException.class).verify();
        when(users.findById(UUID.randomUUID())).thenReturn(Mono.empty());
        UUID missing = UUID.randomUUID();
        when(users.findById(missing)).thenReturn(Mono.empty());
        StepVerifier.create(roles.assignRoles(missing, new RoleAssignmentRequest("UNDERWRITER")))
                .expectError(ResourceNotFoundException.class).verify();

        current.setAccountStatus("ACTIVE");
        UserStatusService statuses = new UserStatusService(users, template, security);
        StepVerifier.create(statuses.updateStatus(current.getUserId(), new UserStatusRequest("suspended")))
                .expectNextCount(1).verifyComplete();
        current.setAccountStatus("SUSPENDED");
        StepVerifier.create(statuses.updateStatus(current.getUserId(), new UserStatusRequest(null)))
                .expectNextCount(1).verifyComplete();
        current.setAccountStatus("SUSPENDED");
        StepVerifier.create(statuses.updateStatus(current.getUserId(), new UserStatusRequest(" ")))
                .expectNextCount(1).verifyComplete();
        current.setAccountStatus("SUSPENDED");
        StepVerifier.create(statuses.updateStatus(current.getUserId(), new UserStatusRequest("SUSPENDED")))
                .expectError(BusinessException.class).verify();
        StepVerifier.create(statuses.updateStatus(current.getUserId(), new UserStatusRequest("UNKNOWN")))
                .expectError(BusinessException.class).verify();
        StepVerifier.create(statuses.updateStatus(missing, new UserStatusRequest("ACTIVE")))
                .expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void administrationQueriesNormalizeFiltersAndSortRecords() {
        UserAccount first = user("UNDERWRITER", "ACTIVE");
        UserAccount second = user("POLICYHOLDER", "INACTIVE");
        second.setEmail("second@example.com");
        second.setCreatedAt(null);
        when(users.findAll()).thenReturn(Flux.just(first, second));
        UserAccountMapper mapper = mock(UserAccountMapper.class);
        when(mapper.toUserResponse(any())).thenReturn(mock(UserResponse.class));
        UserAccountService service = new UserAccountService(users, mock(org.springframework.security.crypto.password.PasswordEncoder.class), mapper, template);
        StepVerifier.create(service.getUsersForAdministration(null, null, null)).expectNextCount(2).verifyComplete();
        StepVerifier.create(service.getUsersForAdministration(" ", " ", null)).expectNextCount(2).verifyComplete();
        StepVerifier.create(service.getUsersForAdministration("ROLE_UNDERWRITER", "ACTIVE", "user"))
                .expectNextCount(1).verifyComplete();
        StepVerifier.create(service.getUsersForAdministration("POLICYHOLDER", "INACTIVE", "missing"))
                .verifyComplete();
        first.setCreatedAt(null);
        second.setCreatedAt(LocalDateTime.now());
        first.setEmail(null);
        first.setDisplayName(null);
        StepVerifier.create(service.getUsersForAdministration(null, null, "holder"))
                .expectNextCount(1).verifyComplete();
        first.setRole(null);
        first.setCreatedAt(null);
        second.setCreatedAt(null);
        StepVerifier.create(service.getUsersForAdministration("POLICYHOLDER", null, null))
                .expectNextCount(2).verifyComplete();
        StepVerifier.create(service.getUsersForAdministration(null, null, "zzz"))
                .verifyComplete();
        first.setRole(" ");
        StepVerifier.create(service.getUsersForAdministration("POLICYHOLDER", null, null))
                .expectNextCount(2).verifyComplete();
        first.setRole("UNDERWRITER");
        first.setCreatedAt(null);
        second.setCreatedAt(LocalDateTime.now());
        StepVerifier.create(service.getUsersForAdministration(null, null, null))
                .expectNextCount(2).verifyComplete();
        first.setCreatedAt(LocalDateTime.now());
        second.setCreatedAt(null);
        StepVerifier.create(service.getUsersForAdministration(null, null, null))
                .expectNextCount(2).verifyComplete();
        second.setAccountStatus("ACTIVE");
        first.setAccountStatus(null);
        StepVerifier.create(service.getUsersForAdministration(null, "ACTIVE", null))
                .expectNextCount(1).verifyComplete();
        when(users.findByRole("POLICYHOLDER")).thenReturn(Flux.just(second));
        StepVerifier.create(service.getUsersByRole(null)).expectNextCount(1).verifyComplete();
        when(users.findByEmail("second@example.com")).thenReturn(Mono.just(second));
        when(mapper.toUserResponse(second)).thenReturn(mock(UserResponse.class));
        StepVerifier.create(service.getUserByEmail("second@example.com")).expectNextCount(1).verifyComplete();
    }
}
