package com.intellisure.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.assertj.core.api.Assertions.assertThat;

class EurekaApplicationConfigurationTest {

    @Test
    void applicationEnablesSpringBootAndEurekaServer() {
        assertThat(EurekaApplication.class)
                .hasAnnotation(SpringBootApplication.class);
        assertThat(EurekaApplication.class)
                .hasAnnotation(EnableEurekaServer.class);
    }
}
