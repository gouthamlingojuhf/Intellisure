package com.intellisure.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

class ApiGatewayApplicationMainTest {

    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ApiGatewayApplication.main(new String[]{"--test-mode"});

            springApplication.verify(() ->
                    SpringApplication.run(ApiGatewayApplication.class, "--test-mode"));
        }
    }
}
