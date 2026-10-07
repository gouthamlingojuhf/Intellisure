package com.intellisure.vendorpartnerservice.controller;

import com.intellisure.vendorpartnerservice.dto.VendorListResponse;
import com.intellisure.vendorpartnerservice.dto.VendorResponse;
import com.intellisure.vendorpartnerservice.dto.VendorSearchRequest;
import com.intellisure.vendorpartnerservice.service.VendorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VendorControllerTest")
class VendorControllerTest {

    @Mock
    private VendorService vendorService;

    @InjectMocks
    private VendorController controller;

    @Test
    @DisplayName("GET /api/vendors/recommendations delegates to vendorService.recommendVendors")
    void getRecommendationsDelegatesToService() {
        VendorResponse vendorResponse = new VendorResponse(
                UUID.randomUUID(), "Restoration Pros", "Restoration Pros", "PROPERTY_RESTORATION",
                List.of("RESTORATION"), List.of("CLEANUP"), List.of("METRO"),
                "VERIFIED", "ACTIVE", "555-0100", "info@restore.com"
        );
        VendorListResponse listResponse = new VendorListResponse(List.of(vendorResponse), 0, 20, 1L);

        when(vendorService.recommendVendors(any(VendorSearchRequest.class))).thenReturn(Mono.just(listResponse));

        StepVerifier.create(controller.getRecommendations("RESTORATION", "METRO", null, "CLEANUP", "ACTIVE", 0, 20))
                .assertNext(res -> {
                    assertEquals(1, res.items().size());
                    assertEquals("Restoration Pros", res.items().get(0).displayName());
                })
                .verifyComplete();

        verify(vendorService).recommendVendors(any(VendorSearchRequest.class));
    }
}
