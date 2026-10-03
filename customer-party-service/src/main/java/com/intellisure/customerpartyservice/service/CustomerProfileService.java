package com.intellisure.customerpartyservice.service;

import com.intellisure.customerpartyservice.dto.CustomerResponse;
import com.intellisure.customerpartyservice.dto.UpdateCustomerProfileRequest;
import com.intellisure.customerpartyservice.entity.BusinessCustomer;
import com.intellisure.customerpartyservice.event.CustomerProfileChangedEvent;
import com.intellisure.customerpartyservice.event.EventPublisher;
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
    private final EventPublisher eventPublisher;

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
                            .version(0L)
                            .createdAt(LocalDateTime.now())
                            .isNew(true)
                            .build();
                    return Mono.just(newCustomer);
                }))
                .flatMap(customer -> {
                    LocalDateTime now = LocalDateTime.now();
                    Long newVersion = (customer.getVersion() == null ? 0L : customer.getVersion()) + 1;
                    boolean isMaterialChange = detectMaterialChange(customer, request);

                    // Track field-level changes for audit
                    emitFieldChanges(customer, request, userId, newVersion, now, isMaterialChange);

                    customer.setBusinessName(request.businessName());
                    customer.setOwnerName(request.ownerName());
                    customer.setPhone(request.phone());
                    customer.setBusinessType(request.businessType());
                    customer.setAddress(request.address());
                    customer.setCity(request.city());
                    customer.setState(request.state());
                    customer.setCountry(request.country());
                    customer.setPostalCode(request.postalCode());
                    customer.setVersion(newVersion);
                    customer.setUpdatedAt(now);

                    if (isMaterialChange) {
                        customer.setLastMaterialChangeAt(now);
                        customer.setMaterialChangePending(true);
                    }

                    if (!customer.isNew()) {
                        customer.setNew(false);
                    }

                    return customerRepository.save(customer);
                })
                .map(customerMapper::toCustomerResponse);
    }

    private boolean detectMaterialChange(BusinessCustomer customer, UpdateCustomerProfileRequest request) {
        return !safeEquals(customer.getBusinessName(), request.businessName())
                || !safeEquals(customer.getOwnerName(), request.ownerName())
                || !safeEquals(customer.getBusinessType(), request.businessType())
                || !safeEquals(customer.getPhone(), request.phone())
                || !safeEquals(customer.getAddress(), request.address())
                || !safeEquals(customer.getCity(), request.city())
                || !safeEquals(customer.getState(), request.state())
                || !safeEquals(customer.getCountry(), request.country())
                || !safeEquals(customer.getPostalCode(), request.postalCode());
    }

    private void emitFieldChanges(BusinessCustomer customer, UpdateCustomerProfileRequest request,
                                  UUID userId, Long newVersion, LocalDateTime now, boolean isMaterialChange) {
        if (!isMaterialChange) {
            return;
        }

        emitIfChanged(customer.getBusinessName(), request.businessName(), "businessName", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getOwnerName(), request.ownerName(), "ownerName", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getBusinessType(), request.businessType(), "businessType", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getPhone(), request.phone(), "phone", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getAddress(), request.address(), "address", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getCity(), request.city(), "city", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getState(), request.state(), "state", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getCountry(), request.country(), "country", customer.getCustomerId(), userId, newVersion, now);
        emitIfChanged(customer.getPostalCode(), request.postalCode(), "postalCode", customer.getCustomerId(), userId, newVersion, now);
    }

    private void emitIfChanged(String oldValue, String newValue, String fieldName,
                               UUID customerId, UUID userId, Long newVersion, LocalDateTime now) {
        if (!safeEquals(oldValue, newValue)) {
            CustomerProfileChangedEvent event = new CustomerProfileChangedEvent(
                    customerId,
                    userId,
                    "MATERIAL_CHANGE",
                    fieldName,
                    oldValue,
                    newValue,
                    newVersion,
                    now
            );
            eventPublisher.publishCustomerProfileChanged(event).subscribe();
        }
    }

    private boolean safeEquals(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equals(b.trim());
    }
}
