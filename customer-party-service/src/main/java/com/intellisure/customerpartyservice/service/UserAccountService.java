package com.intellisure.customerpartyservice.service;



import com.intellisure.customerpartyservice.controller.AuthController;
import com.intellisure.customerpartyservice.dto.RegisterRequest;
import com.intellisure.customerpartyservice.dto.UserResponse;
import com.intellisure.customerpartyservice.entity.UserAccount;
import com.intellisure.customerpartyservice.exception.DuplicateResourceException;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.mapper.UserAccountMapper;
import com.intellisure.customerpartyservice.repository.UserAccountRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepo userAccountRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountMapper userAccountMapper;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);


    public Mono<UserResponse> register(RegisterRequest request) {
        log.info("Register request : {}", request);
        return userAccountRepo.findByEmail(request.email())
                .flatMap(existingUser->
                        Mono.<UserResponse>error(
                                new DuplicateResourceException(
                                        "User with email already exists"
                                )

                        )
                )
                .switchIfEmpty(
                        Mono.defer(()->{
                                    LocalDateTime now = LocalDateTime.now();
                                    UserAccount userAccount = UserAccount.builder()
                                            .userId(UUID.randomUUID())
                                            .email(request.email())
                                            .passwordHash(passwordEncoder.encode(request.password()))
                                            .role("POLICYHOLDER")
                                            .accountStatus("ACTIVE")
                                            .displayName(request.displayName())
                                            .createdAt(now)
                                            .updatedAt(now)
                                            .build();
                                    return r2dbcEntityTemplate.insert(userAccount).map(userAccountMapper::toUserResponse);
                                })
                );
    }

    public Mono<UserResponse> getUserByEmail(String email) {
        return userAccountRepo.findByEmail(email)
                .map(userAccountMapper::toUserResponse);
    }

    public Mono<UserResponse> getUserById(UUID id) {
        return userAccountRepo.findById(id).map(userAccountMapper::toUserResponse)
                .switchIfEmpty(
                        Mono.error(
                                new ResourceNotFoundException("User Not Found")
                        )
                );
    }


}
