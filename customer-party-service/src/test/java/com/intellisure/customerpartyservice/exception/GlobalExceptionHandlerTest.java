package com.intellisure.customerpartyservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleDuplicateResourceProducesConflict() {
        DuplicateResourceException ex = new DuplicateResourceException("dup");
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/x").build());

        var resp = handler.handleDuplicateResource(ex, exchange);

        assertEquals(HttpStatus.CONFLICT, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertEquals("dup", body.message());
        assertEquals("/x", body.path());
    }

    @Test
    void handleResourceNotFoundProducesNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("notfound");
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/y").build());

        var resp = handler.handleResourceNotFound(ex, exchange);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertEquals("notfound", body.message());
        assertEquals("/y", body.path());
    }

    @Test
    void handleInvalidCredentialsProducesUnauthorized() {
        InvalidCredentialsException ex = new InvalidCredentialsException("bad");
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/z").build());

        var resp = handler.handleInvalidCredentials(ex, exchange);

        assertEquals(HttpStatus.UNAUTHORIZED, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertEquals("bad", body.message());
        assertEquals("/z", body.path());
    }

    @Test
    void handleUnexpectedExceptionProducesServerError() {
        Exception ex = new Exception("boom");
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/u").build());

        var resp = handler.handleUnexpectedException(ex, exchange);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        ApiError body = resp.getBody();
        assertEquals("An unexpected error occurred", body.message());
        assertEquals("/u", body.path());
    }
}
