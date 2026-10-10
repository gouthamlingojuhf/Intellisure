package com.intellisure.customerpartyservice;

import com.intellisure.customerpartyservice.controller.*;
import com.intellisure.customerpartyservice.converter.*;
import com.intellisure.customerpartyservice.dto.*;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.event.CustomerProfileChangedEvent;
import com.intellisure.customerpartyservice.event.EventPublisherConfig;
import com.intellisure.customerpartyservice.event.EventPublisher;
import com.intellisure.customerpartyservice.filter.CorrelationIdFilter;
import com.intellisure.customerpartyservice.mapper.BusinessCustomerMapper;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.security.SecurityActorService;
import com.intellisure.customerpartyservice.service.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerPartyInfrastructureCoverageTest {
    private final UUID userId = UUID.randomUUID();

    private Jwt jwt(String subject, Map<String, Object> claims) {
        return new Jwt("token", Instant.now(), Instant.now().plusSeconds(300),
                Map.of("alg", "HS256"), withSubject(subject, claims));
    }

    private Map<String, Object> withSubject(String subject, Map<String, Object> claims) {
        var copy = new java.util.HashMap<>(claims);
        copy.put("sub", subject);
        return copy;
    }

    @Test
    void convertsUuidsAndExtractsEveryJwtRoleShape() {
        UUID value = UUID.randomUUID();
        byte[] bytes = new UuidToBytesConverter().convert(value);
        assertEquals(value, new BytesToUuidConverter().convert(bytes));
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        assertEquals("ROLE_POLICYHOLDER", converter.convert(jwt(userId.toString(), Map.of("roles", List.of("POLICYHOLDER")))).block().getAuthorities().iterator().next().getAuthority());
        assertEquals(2, converter.convert(jwt(userId.toString(), Map.of("roles", "ROLE_ADMIN, UNDERWRITER"))).block().getAuthorities().size());
        assertEquals(1, converter.convert(jwt(userId.toString(), Map.of("roles", " , ROLE_ADMIN "))).block().getAuthorities().size());
        assertEquals(2, converter.convert(jwt(userId.toString(), Map.of("roles", List.of("", "ADMIN")))).block().getAuthorities().size());
        assertEquals("ROLE_VENDOR_MANAGER", converter.convert(jwt(userId.toString(), Map.of("role", "VENDOR_MANAGER"))).block().getAuthorities().iterator().next().getAuthority());
        assertTrue(converter.convert(jwt(userId.toString(), Map.of("role", " "))).block().getAuthorities().isEmpty());
        assertTrue(converter.convert(jwt(userId.toString(), Map.of())).block().getAuthorities().isEmpty());
    }

    @Test
    void securityActorReadsJwtOwnershipAndRoles() {
        SecurityActorService service = new SecurityActorService();
        UUID customerId = UUID.randomUUID();
        JwtAuthenticationToken auth = new JwtAuthenticationConverter().convert(
                jwt(userId.toString(), Map.of("customerId", customerId.toString(), "role", "POLICYHOLDER"))).block();
        var context = new SecurityContextImpl(auth);
        StepVerifier.create(service.currentUserId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))))
                .expectNext(userId).verifyComplete();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))))
                .expectNext(customerId).verifyComplete();
        StepVerifier.create(service.hasRole("POLICYHOLDER").contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))))
                .expectNext(true).verifyComplete();
        StepVerifier.create(service.hasRole("ROLE_POLICYHOLDER").contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))))
                .expectNext(true).verifyComplete();
        StepVerifier.create(service.currentUserId()).expectError().verify();
        JwtAuthenticationToken missingCustomer = new JwtAuthenticationConverter().convert(
                jwt(userId.toString(), Map.of("role", "POLICYHOLDER"))).block();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(missingCustomer)))))
                .expectError().verify();
        JwtAuthenticationToken blankCustomer = new JwtAuthenticationConverter().convert(
                jwt(userId.toString(), Map.of("customerId", " ", "role", "POLICYHOLDER"))).block();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(blankCustomer)))))
                .expectError().verify();
        JwtAuthenticationToken invalidCustomer = new JwtAuthenticationConverter().convert(
                jwt(userId.toString(), Map.of("customerId", "bad", "role", "POLICYHOLDER"))).block();
        StepVerifier.create(service.currentCustomerId().contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(new SecurityContextImpl(invalidCustomer)))))
                .expectError().verify();
    }

    @Test
    void correlationFilterPreservesOrCreatesHeader() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        AtomicReference<String> observed = new AtomicReference<>();
        WebFilterChain chain = exchange -> {
            observed.set(exchange.getRequest().getHeaders().getFirst(CorrelationIdFilter.H));
            return Mono.empty();
        };
        MockServerWebExchange existing = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, "corr-1").build());
        StepVerifier.create(filter.filter(existing, chain)).verifyComplete();
        assertEquals("corr-1", observed.get());
        MockServerWebExchange generated = MockServerWebExchange.from(MockServerHttpRequest.get("/").build());
        StepVerifier.create(filter.filter(generated, chain)).verifyComplete();
        assertNotNull(observed.get());
        assertEquals(observed.get(), generated.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
        MockServerWebExchange blank = MockServerWebExchange.from(MockServerHttpRequest.get("/").header(CorrelationIdFilter.H, " ").build());
        StepVerifier.create(filter.filter(blank, chain)).verifyComplete();
        assertNotNull(blank.getResponse().getHeaders().getFirst(CorrelationIdFilter.H));
    }

    @Test
    void mapsEntitiesAndCreatesEventSink() {
        BusinessCustomer customer = BusinessCustomer.builder().customerId(UUID.randomUUID()).userId(userId)
                .businessName("Acme").ownerName("Owner").businessType("Retail").version(1L)
                .materialChangePending(false).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        UserAccount user = UserAccount.builder().userId(userId).email("a@b.com").role("POLICYHOLDER")
                .accountStatus("ACTIVE").displayName("Owner").createdAt(LocalDateTime.now()).build();
        assertEquals(customer.getCustomerId(), Mappers.getMapper(BusinessCustomerMapper.class).toCustomerResponse(customer).customerId());
        assertTrue(customer.isNew());
        assertEquals(customer.getCustomerId(), customer.getId());
        assertEquals(user.getUserId(), Mappers.getMapper(UserAccountMapper.class).toUserResponse(user).userId());
        assertNull(Mappers.getMapper(BusinessCustomerMapper.class).toCustomerResponse(null));
        assertNull(Mappers.getMapper(UserAccountMapper.class).toUserResponse(null));
        CustomerProfileChangedEvent event = new CustomerProfileChangedEvent(customer.getCustomerId(), userId,
                "MATERIAL_CHANGE", "businessName", "Old", "New", 2L, LocalDateTime.now());
        assertEquals("businessName", event.fieldName());
        assertNotNull(new EventPublisherConfig().customerProfileChangedEventSink());
        EventPublisher publisher = new EventPublisher(new EventPublisherConfig().customerProfileChangedEventSink());
        StepVerifier.create(publisher.publishCustomerProfileChanged(event)).verifyComplete();
        assertNotNull(publisher.getCustomerProfileChangedEventSink());
        Sinks.Many<CustomerProfileChangedEvent> failedSink = mock(Sinks.Many.class);
        when(failedSink.tryEmitNext(any())).thenReturn(Sinks.EmitResult.FAIL_NON_SERIALIZED);
        StepVerifier.create(new EventPublisher(failedSink).publishCustomerProfileChanged(event)).verifyComplete();
        new CustomerPartyServiceApplication();
    }

    @Test
    void exercisesAllCustomerDtoRecordContracts() throws Exception {
        String[] names = {"AdminUserRequest", "AdminUserResponse", "CustomerResponse", "LoginRequest", "LoginResponse",
                "RegisterRequest", "RoleAssignmentRequest", "RoleAssignmentResponse", "UpdateCustomerProfileRequest",
                "UserResponse", "UserStatusRequest", "UserStatusResponse"};
        for (String simple : names) {
            Class<?> type = Class.forName("com.intellisure.customerpartyservice.dto." + simple);
            for (Constructor<?> constructor : type.getDeclaredConstructors()) {
                Object[] arguments = java.util.Arrays.stream(constructor.getParameterTypes())
                        .map(CustomerPartyInfrastructureCoverageTest::defaultValue)
                        .toArray();
                Object value = constructor.newInstance(arguments);
                if (type.isRecord()) {
                    for (var component : type.getRecordComponents()) {
                        type.getMethod(component.getName()).invoke(value);
                    }
                }
            }
        }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }

    @Test
    void delegatesRemainingControllers() {
        Jwt jwt = jwt(userId.toString(), Map.of());
        CustomerProfileService profile = mock(CustomerProfileService.class);
        CustomerResponse customerResponse = mock(CustomerResponse.class);
        when(profile.getCustomerProfileByUserId(userId)).thenReturn(Mono.just(customerResponse));
        when(profile.getCustomerProfileByCustomerId(userId)).thenReturn(Mono.just(customerResponse));
        when(profile.updateCustomerProfile(any(), any())).thenReturn(Mono.just(customerResponse));
        CustomerProfileController profileController = new CustomerProfileController(profile);
        StepVerifier.create(profileController.getMyProfile(jwt)).expectNext(customerResponse).verifyComplete();
        StepVerifier.create(profileController.getCustomerById(userId)).expectNext(customerResponse).verifyComplete();
        StepVerifier.create(profileController.updateMyProfile(jwt, mock(UpdateCustomerProfileRequest.class))).expectNext(customerResponse).verifyComplete();

        UserAccountService account = mock(UserAccountService.class);
        UserResponse userResponse = mock(UserResponse.class);
        when(account.getUserById(userId)).thenReturn(Mono.just(userResponse));
        when(account.getUsersByRole(any())).thenReturn(reactor.core.publisher.Flux.just(userResponse));
        when(account.findAvailableEmployees(any(), any())).thenReturn(reactor.core.publisher.Flux.just(userResponse));
        UserAccountController accountController = new UserAccountController(account);
        StepVerifier.create(accountController.getCurrentUser(jwt)).expectNext(userResponse).verifyComplete();
        StepVerifier.create(accountController.getAvailableClaimsAdjusters()).expectNext(userResponse).verifyComplete();
        StepVerifier.create(accountController.getUsersByRole("UNDERWRITER")).expectNext(userResponse).verifyComplete();

        UserRoleService roles = mock(UserRoleService.class);
        UserStatusService statuses = mock(UserStatusService.class);
        RoleAssignmentResponse role = mock(RoleAssignmentResponse.class);
        UserStatusResponse status = mock(UserStatusResponse.class);
        when(roles.assignRoles(any(), any())).thenReturn(Mono.just(role));
        when(statuses.updateStatus(any(), any())).thenReturn(Mono.just(status));
        StepVerifier.create(new UserRoleController(roles).assignRoles(userId, mock(RoleAssignmentRequest.class))).expectNextCount(1).verifyComplete();
        StepVerifier.create(new UserStatusController(statuses).updateStatus(userId, mock(UserStatusRequest.class))).expectNextCount(1).verifyComplete();

        AuthService auth = mock(AuthService.class);
        LoginResponse login = mock(LoginResponse.class);
        when(auth.login(any())).thenReturn(Mono.just(login));
        when(account.register(any())).thenReturn(Mono.just(userResponse));
        AuthController authController = new AuthController(auth, account);
        StepVerifier.create(authController.register(mock(RegisterRequest.class))).expectNext(userResponse).verifyComplete();
        StepVerifier.create(authController.login(mock(LoginRequest.class))).expectNext(login).verifyComplete();
        StepVerifier.create(authController.me(jwt)).expectNext(userResponse).verifyComplete();

        AdminUserService admin = mock(AdminUserService.class);
        AdminUserResponse adminResponse = mock(AdminUserResponse.class);
        when(admin.registerAdminUser(any())).thenReturn(Mono.just(adminResponse));
        when(account.getUsersForAdministration(any(), any(), any())).thenReturn(reactor.core.publisher.Flux.just(userResponse));
        AdminUserController adminController = new AdminUserController(admin, account);
        StepVerifier.create(adminController.listUsers(null, null, null)).expectNext(userResponse).verifyComplete();
        StepVerifier.create(adminController.createAdminUser(mock(AdminUserRequest.class))).expectNextCount(1).verifyComplete();
    }
}
