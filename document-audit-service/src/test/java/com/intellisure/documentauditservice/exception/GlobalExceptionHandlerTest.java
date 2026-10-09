package com.intellisure.documentauditservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAccessDeniedToForbidden() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleAccessDenied(
                new AccessDeniedBusinessException("denied"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().message()).isEqualTo("denied");
    }

    @Test
    void mapsDocumentNotFoundToNotFound() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleNotFound(
                new DocumentNotFoundException("missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).isEqualTo("missing");
    }

    @Test
    void mapsUnexpectedErrorToInternalServerError() {
        ResponseEntity<GlobalExceptionHandler.ApiError> response = handler.handleUnexpected(
                new IllegalStateException("boom"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred");
    }
}
