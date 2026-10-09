package com.intellisure.eureka;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class EurekaApplicationMainTest {

    @Test
    void delegatesStartupToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            EurekaApplication.main(new String[]{"--test-mode"});

            springApplication.verify(() ->
                    SpringApplication.run(EurekaApplication.class, "--test-mode"));
        }
    }
}
