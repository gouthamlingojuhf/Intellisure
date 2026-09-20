package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.LoginRequest;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.InvalidCredentialsException;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserAccountRepo repository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService service;

    @Test
    void activeUserWithValidPasswordReceivesBearerToken() {
        UUID id = UUID.randomUUID();
        UserAccount user = UserAccount.builder().userId(id).email("owner@example.com")
                .passwordHash("encoded").role("POLICYHOLDER").accountStatus("ACTIVE").build();
        when(repository.findByEmail("owner@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        when(jwtService.generateToken(id, "POLICYHOLDER")).thenReturn("jwt");
        StepVerifier.create(service.login(new LoginRequest("owner@example.com", "secret")))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("jwt", result.accessToken());
                    org.junit.jupiter.api.Assertions.assertEquals("Bearer", result.tokenType());
                }).verifyComplete();
    }

    @Test
    void unknownOrInactiveUserCannotAuthenticate() {
        when(repository.findByEmail("missing@example.com")).thenReturn(Mono.empty());
        StepVerifier.create(service.login(new LoginRequest("missing@example.com", "secret")))
                .expectError(InvalidCredentialsException.class).verify();
        UserAccount inactive = UserAccount.builder().userId(UUID.randomUUID()).email("inactive@example.com")
                .passwordHash("encoded").role("POLICYHOLDER").accountStatus("SUSPENDED").build();
        when(repository.findByEmail(inactive.getEmail())).thenReturn(Mono.just(inactive));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);
        StepVerifier.create(service.login(new LoginRequest(inactive.getEmail(), "secret")))
                .expectError(InvalidCredentialsException.class).verify();
    }

    @Test
    void activeUserWithWrongPasswordCannotAuthenticate() {
        UserAccount user = UserAccount.builder().userId(UUID.randomUUID())
                .email("owner@example.com").passwordHash("encoded")
                .role("POLICYHOLDER").accountStatus("ACTIVE").build();
        when(repository.findByEmail(user.getEmail())).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        StepVerifier.create(service.login(new LoginRequest(user.getEmail(), "wrong")))
                .expectError(InvalidCredentialsException.class).verify();
        verifyNoInteractions(jwtService);
    }
}
