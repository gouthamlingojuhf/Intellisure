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
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    void recommendationDoesNotAutomaticallyCreateAssignment() {
        Vendor verifiedActive = Vendor.builder()
                .vendorId(UUID.randomUUID())
                .legalName("Certified Restoration Co")
                .displayName("Certified Restoration")
                .vendorType(VendorType.PROPERTY_RESTORATION)
                .serviceTypes(List.of("RESTORATION"))
                .capabilities(List.of("WATER_EXTRACTION"))
                .serviceAreas(List.of("METRO_AREA"))
                .contactName("Sarah Agent")
                .verificationStatus(VendorVerificationStatus.VERIFIED)
                .activeStatus(VendorActiveStatus.ACTIVE)
                .build();

        when(vendorRepository.findByServiceTypesContaining("RESTORATION")).thenReturn(Flux.just(verifiedActive));

        // Call recommendation endpoint
        com.intellisure.vendorpartnerservice.dto.VendorSearchRequest request =
                new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(
                        "RESTORATION", null, null, null, null, 0, 10);

        StepVerifier.create(service.recommendVendors(request))
                .assertNext(response -> {
                    org.junit.jupiter.api.Assertions.assertEquals(1, response.items().size());
                    org.junit.jupiter.api.Assertions.assertEquals("Certified Restoration Co", response.items().get(0).legalName());
                })
                .verifyComplete();

        // Vendor repository only read, no mutating calls
        verify(vendorRepository).findByServiceTypesContaining("RESTORATION");
        verify(vendorRepository, never()).save(any());
    }

    @Test
    void searchesByCapabilityLocationAndAllVendorsWithEligibilityFiltering() {
        Vendor eligible = vendor(VendorVerificationStatus.VERIFIED, VendorActiveStatus.ACTIVE);
        Vendor inactive = vendor(VendorVerificationStatus.VERIFIED, VendorActiveStatus.INACTIVE);
        when(vendorRepository.findByServiceTypesContaining("FIRE")).thenReturn(Flux.just(eligible));
        when(vendorRepository.findByServiceAreasContaining("NORTH")).thenReturn(Flux.just(eligible, inactive));
        when(vendorRepository.findAll()).thenReturn(Flux.just(eligible, inactive));
        StepVerifier.create(service.searchVendors(new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(null, null, null, "FIRE", null, null, null)))
                .assertNext(r -> { assertEquals(0, r.page()); assertEquals(20, r.size()); assertEquals(1, r.items().size()); }).verifyComplete();
        StepVerifier.create(service.searchVendors(new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(null, "NORTH", null, null, null, 1, 2)))
                .assertNext(r -> { assertEquals(1, r.page()); assertEquals(2, r.size()); assertEquals(1, r.items().size()); }).verifyComplete();
        StepVerifier.create(service.searchVendors(new com.intellisure.vendorpartnerservice.dto.VendorSearchRequest(null, null, null, null, null, 0, 20)))
                .assertNext(r -> assertEquals(1, r.items().size())).verifyComplete();
    }

    @Test
    void updatesAllVendorFieldsAndHandlesMissingOrInvalidStatus() {
        Vendor vendor = vendor(VendorVerificationStatus.PENDING, VendorActiveStatus.INACTIVE);
        when(vendorRepository.findById(vendor.getVendorId())).thenReturn(Mono.just(vendor));
        when(vendorRepository.save(vendor)).thenReturn(Mono.just(vendor));
        var request = new com.intellisure.vendorpartnerservice.dto.UpdateVendorRequest(
                "Updated", List.of("A"), List.of("B"), List.of("C"), "Name", "Phone", "Email");
        StepVerifier.create(service.updateVendor(vendor.getVendorId(), request)).assertNext(r -> assertEquals("Updated", r.displayName())).verifyComplete();
        StepVerifier.create(service.updateVendorStatus(vendor.getVendorId(), new com.intellisure.vendorpartnerservice.dto.UpdateVendorStatusRequest("ACTIVE", "ready")))
                .assertNext(r -> assertEquals("ACTIVE", r.activeStatus())).verifyComplete();
        StepVerifier.create(service.updateVendorStatus(vendor.getVendorId(), new com.intellisure.vendorpartnerservice.dto.UpdateVendorStatusRequest("UNKNOWN", null)))
                .expectError(IllegalArgumentException.class).verify();
        when(vendorRepository.findById(vendor.getVendorId())).thenReturn(Mono.empty());
        StepVerifier.create(service.getVendor(vendor.getVendorId())).expectError(IllegalArgumentException.class).verify();
    }

    private Vendor vendor(VendorVerificationStatus verification, VendorActiveStatus active) {
        return Vendor.builder().vendorId(UUID.randomUUID()).legalName("Vendor").displayName("Vendor")
                .vendorType(VendorType.TOWING).serviceTypes(List.of("TOWING")).capabilities(List.of("FIRE"))
                .serviceAreas(List.of("NORTH")).contactName("Name").contactPhone("Phone").contactEmail("Email")
                .verificationStatus(verification).activeStatus(active).build();
    }
}
