package com.intellisure.quotepolicyservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private static final String PATH = "/api/v1/quotes";

    private GlobalExceptionHandler handler;
    private ServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(PATH)
        );
    }

    private ApiError bodyOf(ResponseEntity<ApiError> response) {
        assertNotNull(response.getBody());
        return response.getBody();
    }

    @Test
    @DisplayName("maps a resource not found error to 404")
    void mapsNotFound() {
        ResponseEntity<ApiError> response =
                handler.handleResourceNotFound(
                        new ResourceNotFoundException("missing"),
                        exchange
                );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", bodyOf(response).message());
        assertEquals(404, bodyOf(response).status());
        assertEquals("Not Found", bodyOf(response).error());
        assertEquals(PATH, bodyOf(response).path());
        assertNull(bodyOf(response).validationErrors());
        assertNotNull(bodyOf(response).timestamp());
    }

    @Test
    @DisplayName("maps a business error to 400")
    void mapsBusinessError() {
        ResponseEntity<ApiError> response =
                handler.handleBusinessException(
                        new BusinessException("rule violated"),
                        exchange
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("rule violated", bodyOf(response).message());
    }

    @Test
    @DisplayName("maps an access denied business error to 403")
    void mapsAccessDenied() {
        ResponseEntity<ApiError> response =
                handler.handleAccessDeniedBusinessException(
                        new AccessDeniedBusinessException("nope"),
                        exchange
                );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("nope", bodyOf(response).message());
    }

    @Test
    @DisplayName("maps a missing underwriter error to 409")
    void mapsNoUnderwriterAvailable() {
        ResponseEntity<ApiError> response =
                handler.handleNoUnderwriterAvailable(
                        new NoUnderwriterAvailableException("no one"),
                        exchange
                );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("no one", bodyOf(response).message());
    }

    @Test
    @DisplayName("maps a duplicate key error to 409 with a generic message")
    void mapsDuplicateKey() {
        ResponseEntity<ApiError> response = handler.handleDuplicateKey(
                new DuplicateKeyException("unique index violated"),
                exchange
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "A record with the same unique value already exists",
                bodyOf(response).message()
        );
    }

    @Test
    @DisplayName("collects bean validation field errors")
    void mapsValidationErrors() throws Exception {
        BindException bindResult = new BindException(
                new Object(),
                "request"
        );

        bindResult.addError(
                new FieldError(
                        "request",
                        "productCode",
                        "must not be blank"
                )
        );
        bindResult.addError(
                new FieldError(
                        "request",
                        "coverages",
                        "must not be empty"
                )
        );

        WebExchangeBindException bindException =
                new WebExchangeBindException(
                        new MethodParameter(
                                Object.class.getDeclaredMethod("toString"),
                                -1
                        ),
                        bindResult
                );

        ResponseEntity<ApiError> response = handler.handleValidationException(
                bindException,
                exchange
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Request validation failed",
                bodyOf(response).message()
        );
        assertEquals(2, bodyOf(response).validationErrors().size());
        assertEquals(
                "must not be blank",
                bodyOf(response)
                        .validationErrors()
                        .get("productCode")
        );
        assertEquals(
                "must not be empty",
                bodyOf(response)
                        .validationErrors()
                        .get("coverages")
        );
    }

    @Test
    @DisplayName("maps an unparseable request to 400")
    void mapsServerWebInputException() {
        ResponseEntity<ApiError> response =
                handler.handleServerWebInputException(
                        new ServerWebInputException("bad uuid"),
                        exchange
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Invalid request value or format",
                bodyOf(response).message()
        );
    }

    @Test
    @DisplayName("maps an illegal argument to 400 keeping the original message")
    void mapsIllegalArgumentWithMessage() {
        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException("bad argument"),
                        exchange
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad argument", bodyOf(response).message());
    }

    @Test
    @DisplayName("falls back to a generic message for an illegal argument without one")
    void mapsIllegalArgumentWithoutMessage() {
        ResponseEntity<ApiError> response =
                handler.handleIllegalArgumentException(
                        new IllegalArgumentException(),
                        exchange
                );

        assertEquals("Invalid request value", bodyOf(response).message());
    }

    @Test
    @DisplayName("maps an unexpected failure to 500 without leaking details")
    void mapsUnexpectedFailure() {
        ResponseEntity<ApiError> response =
                handler.handleUnexpectedException(
                        new IllegalStateException(
                                "connection string leaked"
                        ),
                        exchange
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );
        assertEquals(
                "An unexpected error occurred",
                bodyOf(response).message()
        );
        assertTrue(
                !bodyOf(response)
                        .message()
                        .contains("leaked")
        );
    }

    @Test
    @DisplayName("maps a downstream failure to 502")
    void mapsDownstreamServiceError() {
        ResponseEntity<ApiError> response =
                handler.handleDownstreamError(
                        new DownstreamServiceException("risk 500"),
                        exchange
                );

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertEquals("risk 500", bodyOf(response).message());
    }

    @Test
    @DisplayName("maps a downstream outage to 503")
    void mapsDownstreamServiceUnavailable() {
        ResponseEntity<ApiError> response =
                handler.handleDownstreamUnavailable(
                        new DownstreamServiceUnavailableException(
                                "breaker open",
                                new RuntimeException("cause")
                        ),
                        exchange
                );

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );
        assertEquals("breaker open", bodyOf(response).message());
    }

    @Test
    @DisplayName("keeps the cause of downstream exceptions")
    void keepsCause() {
        RuntimeException cause = new RuntimeException("cause");

        assertEquals(
                cause,
                new DownstreamServiceException("msg", cause).getCause()
        );
        assertEquals(
                cause,
                new DownstreamServiceUnavailableException("msg", cause)
                        .getCause()
        );
        assertNull(
                new DownstreamServiceException("msg").getCause()
        );
        assertNull(
                new DownstreamServiceUnavailableException("msg").getCause()
        );
    }
}
