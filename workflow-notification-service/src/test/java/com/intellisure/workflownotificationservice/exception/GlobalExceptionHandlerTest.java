package com.intellisure.workflownotificationservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsBusinessAccessDenialToForbidden() {
        var response = handler.handleAccessDenied(new AccessDeniedBusinessException("denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("denied", response.getBody().message());
    }

    @Test
    void mapsUnexpectedErrorsToGenericInternalServerError() {
        var response = handler.handleUnexpected(new IllegalStateException("secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }
}
