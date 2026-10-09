package com.intellisure.documentauditservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class DocumentAuditServiceApplicationMainTest {
    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            DocumentAuditServiceApplication.main(new String[]{"--test-mode"});
            springApplication.verify(() -> SpringApplication.run(
                    DocumentAuditServiceApplication.class, "--test-mode"));
        }
    }
}
