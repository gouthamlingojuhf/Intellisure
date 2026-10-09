package com.intellisure.claimsservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class ClaimsServiceApplicationMainTest {
    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> app = mockStatic(SpringApplication.class)) {
            ClaimsServiceApplication.main(new String[] {"--test-mode"});
            app.verify(() -> SpringApplication.run(ClaimsServiceApplication.class, "--test-mode"));
        }
    }
}
