package com.intellisure.customerpartyservice.repository;

import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusinessCustomerRepository extends ReactiveCrudRepository<BusinessCustomer, UUID> {
    Mono<BusinessCustomer> findByUserId(UUID userId);
}
