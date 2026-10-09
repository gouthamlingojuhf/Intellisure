package com.intellisure.documentauditservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {
    private static final String SECRET = "VVGF9BeSRx3K2epQp4/DRnq881+8YixWG3KHI4qTl9I=";

    @Test
    void createsJwtDecoderFromConfiguredSecret() {
        SecurityConfig config = new SecurityConfig();
        ReflectionTestUtils.setField(config, "jwtSecret", SECRET);

        ReactiveJwtDecoder decoder = config.reactiveJwtDecoder();

        assertThat(decoder).isNotNull();
    }

}
