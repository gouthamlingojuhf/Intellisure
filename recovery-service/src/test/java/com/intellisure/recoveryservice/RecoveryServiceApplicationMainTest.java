package com.intellisure.recoveryservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class RecoveryServiceApplicationMainTest {
    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            RecoveryServiceApplication.main(new String[] {"--test-mode"});
            springApplication.verify(() -> SpringApplication.run(RecoveryServiceApplication.class, "--test-mode"));
        }
    }
}
