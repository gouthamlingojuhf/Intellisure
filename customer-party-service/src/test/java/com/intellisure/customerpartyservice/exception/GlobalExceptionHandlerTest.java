package com.intellisure.customerpartyservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.validation.FieldError;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.List;
import java.util.Set;

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

    @Test
    void handlesValidationAndConstraintErrors() {
        ServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/v").build());
        WebExchangeBindException bind = mock(WebExchangeBindException.class);
        when(bind.getFieldErrors()).thenReturn(List.of(new FieldError("request", "email", "must be valid")));
        var validation = handler.handleValidation(bind, exchange);
        assertEquals(HttpStatus.BAD_REQUEST, validation.getStatusCode());
        assertEquals("email: must be valid", validation.getBody().message());
        var empty = mock(WebExchangeBindException.class);
        when(empty.getFieldErrors()).thenReturn(List.of());
        assertEquals("Invalid request", handler.handleValidation(empty, exchange).getBody().message());
        var constraint = handler.handleConstraintViolation(new ConstraintViolationException(Set.of()), exchange);
        assertEquals(HttpStatus.BAD_REQUEST, constraint.getStatusCode());
        assertEquals("", constraint.getBody().message());
    }
}
