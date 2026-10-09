package com.intellisure.workflownotificationservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class WorkflowNotificationServiceApplicationMainTest {

    @Test
    void mainMethodIsRunnable() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            WorkflowNotificationServiceApplication.main(new String[] {"--test-mode"});
            springApplication.verify(() -> SpringApplication.run(
                    WorkflowNotificationServiceApplication.class, "--test-mode"));
        }
    }
}
