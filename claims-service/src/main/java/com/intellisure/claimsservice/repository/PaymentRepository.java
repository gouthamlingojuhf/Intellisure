package com.intellisure.claimsservice.repository;

import com.intellisure.claimsservice.entity.Payment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepository extends ReactiveCrudRepository<Payment, UUID> {
    Flux<Payment> findAllByClaimId(UUID claimId);
    Mono<Payment> findByPaymentReference(String paymentReference);
}