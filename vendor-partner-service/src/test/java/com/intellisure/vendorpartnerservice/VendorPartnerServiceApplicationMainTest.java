package com.intellisure.vendorpartnerservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class VendorPartnerServiceApplicationMainTest {
    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> app = mockStatic(SpringApplication.class)) {
            VendorPartnerServiceApplication.main(new String[] {"--test-mode"});
            app.verify(() -> SpringApplication.run(VendorPartnerServiceApplication.class, "--test-mode"));
        }
    }
}
