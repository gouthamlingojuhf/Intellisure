package com.intellisure.apigateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("ApiGatewaySecurityTests")
class ApiGatewayApplicationTests {

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint is rejected with 401 Unauthorized")
    void unauthenticatedRequestToProtectedEndpointReturns401() {
        client.get()
                .uri("/api/quotes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Invalid Bearer token is rejected with 401 Unauthorized")
    void invalidBearerTokenReturns401() {
        client.get()
                .uri("/api/quotes")
                .header("Authorization", "Bearer invalid.garbage.jwt")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("OPTIONS preflight from allowed origin succeeds with CORS headers")
    void optionsPreflightAllowed() {
        client.options()
                .uri("/api/claims")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:4200");
    }

    @Test
    @DisplayName("Public auth login endpoint does not require JWT authorization")
    void publicAuthEndpointDoesNotReturn401() {
        client.post()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus().value(status -> {
                    // Gateway allows the request through; downstream gives 503/404, but NOT 401 Unauthorized
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }

    @Test
    @DisplayName("Swagger UI public documentation does not return 401")
    void swaggerDocsPublicEndpointDoesNotReturn401() {
        client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().value(status -> {
                    org.junit.jupiter.api.Assertions.assertNotEquals(401, status);
                });
    }
}
