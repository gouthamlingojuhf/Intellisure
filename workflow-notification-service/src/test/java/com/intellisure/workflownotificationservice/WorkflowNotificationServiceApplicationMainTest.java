package com.intellisure.workflownotificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowNotificationServiceApplicationMainTest {

    @Test
    void mainClassConfigurationIsValid() {
        WorkflowNotificationServiceApplication app = new WorkflowNotificationServiceApplication();
        assertNotNull(app);
        assertTrue(WorkflowNotificationServiceApplication.class.isAnnotationPresent(SpringBootApplication.class));
    }
}
