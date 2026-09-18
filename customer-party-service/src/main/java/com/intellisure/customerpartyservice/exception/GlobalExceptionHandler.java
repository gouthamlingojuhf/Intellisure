package com.intellisure.customerpartyservice.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------------------------------
    // 409 - Duplicate Resource
    // ------------------------------------------------------------

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResource(
            DuplicateResourceException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exchange
        );
    }


    // ------------------------------------------------------------
    // 404 - Resource Not Found
    // ------------------------------------------------------------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                exchange
        );
    }


    // ------------------------------------------------------------
    // 401 - Invalid Credentials
    // ------------------------------------------------------------

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(
            InvalidCredentialsException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                exchange
        );
    }


    // ------------------------------------------------------------
    // 400 - Validation Error
    // ------------------------------------------------------------

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidation(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {

        String message = exception.getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .findFirst()
                .orElse("Invalid request");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                exchange
        );
    }


    // ------------------------------------------------------------
    // 400 - Constraint Violation
    // ------------------------------------------------------------

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                exchange
        );
    }


    // ------------------------------------------------------------
    // 500 - Unexpected Error
    // ------------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                exchange
        );
    }


    // ------------------------------------------------------------
    // Common Response Builder
    // ------------------------------------------------------------

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest()
                        .getPath()
                        .value()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
