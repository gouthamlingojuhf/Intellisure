package com.intellisure.analyticsintelligenceservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class AnalyticsIntelligenceServiceApplicationMainTest {
    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            AnalyticsIntelligenceServiceApplication.main(new String[]{"--test-mode"});
            springApplication.verify(() -> SpringApplication.run(
                    AnalyticsIntelligenceServiceApplication.class, "--test-mode"));
        }
    }
}
