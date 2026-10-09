package com.intellisure.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private static final String SECRET =
            "VVGF9BeSRx3K2epQp4/DRnq881+8YixWG3KHI4qTl9I=";

    private final SecurityConfig config = new SecurityConfig();

    @Test
    void corsConfigurationAllowsConfiguredFrontendOriginsAndCredentials() {
        var source = config.corsConfigurationSource();
        var cors = source.getCorsConfiguration(
                MockServerWebExchange.from(
                        MockServerHttpRequest.get("/api/quotes").build()));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactlyInAnyOrder(
                        "http://localhost:4200",
                        "http://localhost:4201",
                        "http://localhost:4202",
                        "http://localhost:4203",
                        "http://localhost:4205");
        assertThat(cors.getAllowCredentials()).isTrue();
        assertThat(cors.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    void createsHmacJwtSecretKeyFromConfiguredBase64Secret() {
        ReflectionTestUtils.setField(config, "jwtSecret", SECRET);

        SecretKey key = config.jwtSecretKey();

        assertThat(key.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(key.getEncoded()).hasSize(32);
    }

    @Test
    void createsReactiveJwtDecoderFromSecretKey() {
        ReflectionTestUtils.setField(config, "jwtSecret", SECRET);

        ReactiveJwtDecoder decoder = config.jwtDecoder(config.jwtSecretKey());

        assertThat(decoder).isNotNull();
    }
}
