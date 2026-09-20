package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.CustomerResponse;
import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import com.intellisure.customerpartyservice.mapper.BusinessCustomerMapper;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final BusinessCustomerRepository customerRepository;
    private final BusinessCustomerMapper customerMapper;

    public Mono<CustomerResponse> getCustomerProfileByUserId(UUID userId) {
        return customerRepository.findByUserId(userId)
                .map(customerMapper::toCustomerResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer profile not found for user")));
    }

    public Mono<CustomerResponse> updateCustomerProfile(UUID userId, UpdateCustomerProfileRequest request) {
        return customerRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    BusinessCustomer newCustomer = BusinessCustomer.builder()
                            .customerId(UUID.randomUUID())
                            .userId(userId)
                            .createdAt(LocalDateTime.now())
                            .isNew(true)
                            .build();
                    return Mono.just(newCustomer);
                }))
                .flatMap(customer -> {
                    customer.setBusinessName(request.businessName());
                    customer.setOwnerName(request.ownerName());
                    customer.setPhone(request.phone());
                    customer.setBusinessType(request.businessType());
                    customer.setAddress(request.address());
                    customer.setCity(request.city());
                    customer.setState(request.state());
                    customer.setCountry(request.country());
                    customer.setPostalCode(request.postalCode());
                    customer.setUpdatedAt(LocalDateTime.now());
                    
                    if (!customer.isNew()) {
                        customer.setNew(false);
                    }
                    
                    return customerRepository.save(customer);
                })
                .map(customerMapper::toCustomerResponse);
    }
}
