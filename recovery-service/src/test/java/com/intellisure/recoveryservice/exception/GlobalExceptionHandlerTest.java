package com.intellisure.recoveryservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    @Test
    void mapsAccessDeniedAndUnexpectedErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        var denied = handler.handleAccessDenied(new AccessDeniedBusinessException("denied"));
        assertEquals(HttpStatus.FORBIDDEN, denied.getStatusCode());
        assertEquals("denied", denied.getBody().message());
        var unexpected = handler.handleUnexpected(new IllegalStateException("secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, unexpected.getStatusCode());
        assertEquals("An unexpected error occurred", unexpected.getBody().message());
    }
}
