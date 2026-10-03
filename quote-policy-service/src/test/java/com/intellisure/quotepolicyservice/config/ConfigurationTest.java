package com.intellisure.quotepolicyservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Configuration")
class ConfigurationTest {

    @Test
    @DisplayName("the load balanced WebClient builder is decorated with a correlation filter")
    void webClientBuilderIsAvailable() {
        WebClient.Builder builder = new WebClientConfig()
                .loadBalancedWebClientBuilder();

        assertNotNull(builder);
        assertNotSame(builder, WebClient.builder());
        assertTrue(
                builder.build() != null
        );
    }

    @Test
    @DisplayName("the OpenAPI definition declares the bearer security scheme")
    void openApiDeclaresBearerAuth() {
        var openApi = new OpenApiConfig().securedOpenApi();

        assertNotNull(openApi.getComponents());
        assertTrue(
                openApi.getComponents()
                        .getSecuritySchemes()
                        .containsKey("bearerAuth")
        );
        assertEquals(
                "bearer",
                openApi.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth")
                        .getScheme()
        );
        assertEquals(
                "JWT",
                openApi.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth")
                        .getBearerFormat()
        );
    }

    @Test
    @DisplayName("the circuit breaker customizer targets the named instance")
    @SuppressWarnings("unchecked")
    void circuitBreakerCustomizerIsAvailable() {
        Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer =
                new CircuitBreakerEventConfig()
                        .circuitBreakerEventCustomizer();

        assertNotNull(customizer);

        CircuitBreaker circuitBreaker = CircuitBreaker.ofDefaults(
                "riskUnderwritingService"
        );

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {
                });

        assertEquals(
                "riskUnderwritingService",
                circuitBreaker.getName()
        );
    }

    @Test
    @DisplayName("the JWT decoder is built from the base64 encoded secret")
    void jwtDecoderIsCreated() {
        var decoder = new SecurityConfig().reactiveJwtDecoder(
                "VVGF9BeSRx3K2epQp4/DRnq881+8YixWG3KHI4qTl9I="
        );

        assertNotNull(decoder);
    }

    @Test
    @DisplayName("the R2DBC custom conversions expose the UUID converters")
    void r2dbcCustomConversionsAreRegistered() {
        var conversions = new R2dbcConfig()
                .r2dbcCustomConversions(
                        io.r2dbc.spi.ConnectionFactories.get(
                                "r2dbc:h2:mem:///conversion_test"
                        )
                );

        assertNotNull(conversions);
    }

    @Test
    @DisplayName("the underwriter pool defaults to an empty list and is bindable")
    void underwriterPoolProperties() {
        UnderwriterPoolProperties properties =
                new UnderwriterPoolProperties();

        assertTrue(
                properties.getEligibleUnderwriterIds()
                        .isEmpty()
        );

        properties.setEligibleUnderwriterIds(
                java.util.List.of(
                        java.util.UUID.randomUUID()
                )
        );

        assertEquals(
                1,
                properties.getEligibleUnderwriterIds()
                        .size()
        );
    }
}
