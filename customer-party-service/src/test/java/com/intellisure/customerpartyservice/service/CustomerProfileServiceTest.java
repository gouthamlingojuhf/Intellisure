package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.event.EventPublisher;
import com.intellisure.customerpartyservice.mapper.BusinessCustomerMapper;
import com.intellisure.customerpartyservice.repository.BusinessCustomerRepository;
import com.intellisure.customerpartyservice.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {
    @Mock BusinessCustomerRepository repository;
    @Mock BusinessCustomerMapper mapper;
    @Mock EventPublisher eventPublisher;
    @InjectMocks CustomerProfileService service;

    @Test
    void missingProfileIsReported() {
        UUID user = UUID.randomUUID(); when(repository.findByUserId(user)).thenReturn(Mono.empty());
        StepVerifier.create(service.getCustomerProfileByUserId(user)).expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void updatesExistingProfileAndMapsIt() {
        UUID user = UUID.randomUUID();
        BusinessCustomer customer = BusinessCustomer.builder()
                .customerId(UUID.randomUUID())
                .userId(user)
                .version(0L)
                .isNew(false)
                .build();
        when(repository.findByUserId(user)).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));
        when(eventPublisher.publishCustomerProfileChanged(any())).thenReturn(Mono.empty());
        com.intellisure.customerpartyservice.dto.CustomerResponse response =
                new com.intellisure.customerpartyservice.dto.CustomerResponse(customer.getCustomerId(), user, null, null, null, null, null, null, null, null, null, null, null);
        when(mapper.toCustomerResponse(customer)).thenReturn(response);
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest("Acme", "Owner", "1", "Retail", "Street", "City", "State", "US", "1");
        StepVerifier.create(service.updateCustomerProfile(user, request)).expectNext(response).verifyComplete();
        org.junit.jupiter.api.Assertions.assertEquals("Acme", customer.getBusinessName());
        verify(repository).save(customer);
    }
}
