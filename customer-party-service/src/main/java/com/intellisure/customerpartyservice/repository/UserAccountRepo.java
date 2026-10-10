package com.intellisure.customerpartyservice.repository;

import com.intellisure.customerpartyservice.entity.UserAccount;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserAccountRepo extends ReactiveCrudRepository<UserAccount, UUID> {
    Mono<UserAccount> findByEmail(String email);
    Flux<UserAccount> findByRole(String role);
    Flux<UserAccount> findByRoleAndAccountStatus(String role, String accountStatus);
    Flux<UserAccount> findByAccountStatus(String accountStatus);
}
