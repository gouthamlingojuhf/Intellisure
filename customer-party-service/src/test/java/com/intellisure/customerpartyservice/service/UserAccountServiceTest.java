package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.*;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import com.intellisure.customerpartyservice.exception.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {
    @Mock UserAccountRepo repository; @Mock PasswordEncoder encoder; @Mock UserAccountMapper mapper;
    @Mock R2dbcEntityTemplate template; @InjectMocks UserAccountService service;

    @Test
    void registersNewUserAndEncodesPassword() {
        UserAccount saved = UserAccount.builder().userId(UUID.randomUUID()).email("a@b.test").build();
        when(repository.findByEmail("a@b.test")).thenReturn(Mono.empty());
        when(encoder.encode("secret")).thenReturn("hash");
        when(template.insert(any(UserAccount.class))).thenReturn(Mono.just(saved));
        UserResponse response = new UserResponse(saved.getUserId(), saved.getEmail(), null, null, null, null, null);
        when(mapper.toUserResponse(saved)).thenReturn(response);
        StepVerifier.create(service.register(new RegisterRequest("a@b.test", "secret", "A")))
                .expectNext(response).verifyComplete();
        verify(encoder).encode("secret");
    }

    @Test
    void rejectsDuplicateAndMissingUser() {
        UserAccount existing = UserAccount.builder().userId(UUID.randomUUID()).email("a@b.test").build();
        when(repository.findByEmail(existing.getEmail())).thenReturn(Mono.just(existing));
        StepVerifier.create(service.register(new RegisterRequest(existing.getEmail(), "x", "A")))
                .expectError(DuplicateResourceException.class).verify();
        UUID id = UUID.randomUUID(); when(repository.findById(id)).thenReturn(Mono.empty());
        StepVerifier.create(service.getUserById(id)).expectError(ResourceNotFoundException.class).verify();
    }
}
