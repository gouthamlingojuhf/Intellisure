package com.intellisure.vendorpartnerservice.service;

import com.intellisure.vendorpartnerservice.entity.Vendor;
import com.intellisure.vendorpartnerservice.entity.VendorType;
import com.intellisure.vendorpartnerservice.entity.VendorVerificationStatus;
import com.intellisure.vendorpartnerservice.entity.VendorActiveStatus;
import com.intellisure.vendorpartnerservice.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorServiceTest {
    @Mock VendorRepository vendorRepository;
    @InjectMocks VendorService service;

    @Test
    void searchesVendorsReturnsEmptyListWhenNoVendors() {
        when(vendorRepository.findByServiceTypesContaining("TOWING")).thenReturn(Flux.empty());
        StepVerifier.create(service.searchVendors(new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(
                        "TOWING", null, null, null, null, 0, 20)))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.items().size());
                    org.junit.jupiter.api.Assertions.assertEquals(0, response.page());
                    org.junit.jupiter.api.Assertions.assertEquals(20, response.size());
                    org.junit.jupiter.api.Assertions.assertEquals(0L, response.totalElements());
                }).verifyComplete();
    }

    @Test
    void updatesVendorStatus() {
        Vendor vendor = Vendor.builder()
                .vendorId(UUID.randomUUID())
                .legalName("Test Vendor")
                .displayName("Test Vendor")
                .vendorType(VendorType.TOWING)
                .serviceTypes(List.of("TOWING"))
                .capabilities(List.of("TOWING"))
                .serviceAreas(List.of("US"))
                .contactName("Test")
                .contactPhone("555-0000")
                .contactEmail("test@test.com")
                .verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.ACTIVE)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .isNew(false)
                .build();
        when(vendorRepository.findById(vendor.getVendorId())).thenReturn(Mono.just(vendor));
        when(vendorRepository.save(vendor)).thenReturn(Mono.just(vendor));
        StepVerifier.create(service.updateVendorStatus(vendor.getVendorId(), 
                        new com.intellisure.vendorpartnerservice.dto.UpdateVendorStatusRequest("ACTIVE", "Test")))
                .assertNext(v -> org.junit.jupiter.api.Assertions.assertEquals("ACTIVE", v.activeStatus())).verifyComplete();
    }

    @Test
    void searchVendorsFiltersByServiceType() {
        Vendor vendor = Vendor.builder()
                .vendorId(UUID.randomUUID())
                .legalName("Test Vendor")
                .displayName("Test Vendor")
                .vendorType(VendorType.TOWING)
                .serviceTypes(List.of("TOWING"))
                .capabilities(List.of("TOWING"))
                .serviceAreas(List.of("US"))
                .contactName("Test")
                .contactPhone("555-0000")
                .contactEmail("test@test.com")
                .verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.ACTIVE)
                .createdAt(java.time.LocalDateTime.now())
                .updatedAt(java.time.LocalDateTime.now())
                .isNew(false)
                .build();
        when(vendorRepository.findByServiceTypesContaining("TOWING")).thenReturn(Flux.just(vendor));
        StepVerifier.create(service.searchVendors(new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(
                        "TOWING", null, null, null, null, 0, 20)))
                .assertNext(response -> org.junit.jupiter.api.Assertions.assertEquals(1, response.items().size()))
                .verifyComplete();
    }
}