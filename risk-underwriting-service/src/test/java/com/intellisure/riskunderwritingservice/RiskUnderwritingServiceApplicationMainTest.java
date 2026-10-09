package com.intellisure.riskunderwritingservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class RiskUnderwritingServiceApplicationMainTest {
    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> application = mockStatic(SpringApplication.class)) {
            RiskUnderwritingServiceApplication.main(new String[]{"--test-mode"});
            application.verify(() -> SpringApplication.run(RiskUnderwritingServiceApplication.class, "--test-mode"));
        }
    }
}
