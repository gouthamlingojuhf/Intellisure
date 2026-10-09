package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.dto.CustomerResponse;
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

    @Test
    void createsMissingProfileAndDoesNotEmitWhenNothingMaterialChanges() {
        UUID user = UUID.randomUUID();
        when(repository.findByUserId(user)).thenReturn(Mono.empty());
        when(repository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        BusinessCustomerMapper mapper = org.mockito.Mockito.mock(BusinessCustomerMapper.class);
        // InjectMocks already uses the field mapper; replace its behavior for the generated profile.
        CustomerResponse response = new CustomerResponse(null, user, null, null, null, null, null, null, null, null, null, null, null);
        when(mapper.toCustomerResponse(any())).thenReturn(response);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "customerMapper", mapper);
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest(null, null, null, null, null, null, null, null, null);
        StepVerifier.create(service.updateCustomerProfile(user, request)).expectNext(response).verifyComplete();
        verify(eventPublisher, never()).publishCustomerProfileChanged(any());
    }

    @Test
    void unchangedExistingProfileDoesNotBecomeMaterialChange() {
        UUID user = UUID.randomUUID();
        BusinessCustomer customer = BusinessCustomer.builder().customerId(UUID.randomUUID()).userId(user)
                .businessName("Acme").ownerName("Owner").businessType("Retail").phone("1")
                .address("Street").city("City").state("State").country("US").postalCode("1")
                .version(1L).isNew(false).build();
        when(repository.findByUserId(user)).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));
        CustomerResponse response = new CustomerResponse(customer.getCustomerId(), user, "Acme", "Owner", "Retail", "1", "Street", "City", "State", "US", "1", null, null);
        when(mapper.toCustomerResponse(customer)).thenReturn(response);
        UpdateCustomerProfileRequest request = new UpdateCustomerProfileRequest(" Acme ", " Owner ", "1", "Retail", "Street", "City", "State", "US", "1");
        StepVerifier.create(service.updateCustomerProfile(user, request)).expectNext(response).verifyComplete();
        verify(eventPublisher, never()).publishCustomerProfileChanged(any());
    }

    @Test
    void detectsEachIndividualMaterialProfileFieldChange() {
        UUID user = UUID.randomUUID();
        BusinessCustomer customer = BusinessCustomer.builder().customerId(UUID.randomUUID()).userId(user)
                .businessName("Acme").ownerName("Owner").businessType("Retail").phone("1")
                .address("Street").city("City").state("State").country("US").postalCode("1")
                .version(1L).isNew(false).build();
        when(repository.findByUserId(user)).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));
        when(mapper.toCustomerResponse(customer)).thenReturn(mock(CustomerResponse.class));
        when(eventPublisher.publishCustomerProfileChanged(any())).thenReturn(Mono.empty());
        String[][] changes = {
                {"Changed", "Owner", "Retail", "1", "Street", "City", "State", "US", "1"},
                {"Acme", "Changed", "Retail", "1", "Street", "City", "State", "US", "1"},
                {"Acme", "Owner", "Changed", "1", "Street", "City", "State", "US", "1"},
                {"Acme", "Owner", "Retail", "2", "Street", "City", "State", "US", "1"},
                {"Acme", "Owner", "Retail", "1", "Changed", "City", "State", "US", "1"},
                {"Acme", "Owner", "Retail", "1", "Street", "Changed", "State", "US", "1"},
                {"Acme", "Owner", "Retail", "1", "Street", "City", "Changed", "US", "1"},
                {"Acme", "Owner", "Retail", "1", "Street", "City", "State", "CA", "1"},
                {"Acme", "Owner", "Retail", "1", "Street", "City", "State", "US", "2"}
        };
        for (String[] change : changes) {
            customer.setBusinessName("Acme"); customer.setOwnerName("Owner"); customer.setBusinessType("Retail");
            customer.setPhone("1"); customer.setAddress("Street"); customer.setCity("City"); customer.setState("State");
            customer.setCountry("US"); customer.setPostalCode("1");
            StepVerifier.create(service.updateCustomerProfile(user, new UpdateCustomerProfileRequest(
                    change[0], change[1], change[3], change[2], change[4], change[5], change[6], change[7], change[8])))
                    .expectNextCount(1).verifyComplete();
        }
    }

    @Test
    void detectsNullToValueAndValueToNullMaterialChanges() {
        UUID user = UUID.randomUUID();
        BusinessCustomer customer = BusinessCustomer.builder()
                .customerId(UUID.randomUUID()).userId(user).version(null).isNew(false).build();
        when(repository.findByUserId(user)).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));
        when(mapper.toCustomerResponse(customer)).thenReturn(mock(CustomerResponse.class));
        when(eventPublisher.publishCustomerProfileChanged(any())).thenReturn(Mono.empty());

        StepVerifier.create(service.updateCustomerProfile(user,
                        new UpdateCustomerProfileRequest("Acme", null, null, null, null, null, null, null, null)))
                .expectNextCount(1).verifyComplete();

        customer.setBusinessName("Acme");
        StepVerifier.create(service.updateCustomerProfile(user,
                        new UpdateCustomerProfileRequest(null, null, null, null, null, null, null, null, null)))
                .expectNextCount(1).verifyComplete();
    }
}
