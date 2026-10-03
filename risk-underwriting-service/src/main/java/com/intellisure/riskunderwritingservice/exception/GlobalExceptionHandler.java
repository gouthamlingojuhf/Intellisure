package com.intellisure.riskunderwritingservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                exchange,
                null
        );
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(
            BusinessException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                exchange,
                null
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicate(
            DuplicateKeyException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "A record with the same unique value already exists",
                exchange,
                null
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiError> handleValidation(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getFieldErrors().forEach(error ->
                errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                )
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                exchange,
                errors
        );
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleInput(
            ServerWebInputException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid request value or format",
                exchange,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            ServerWebExchange exchange
    ) {
        log.error(
                "Unexpected error on path {}",
                exchange.getRequest().getPath().value(),
                exception
        );

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                exchange,
                null
        );
    }

    @ExceptionHandler(AccessDeniedBusinessException.class)
    public ResponseEntity<ApiError> handleAccessDeniedBusinessException(
            AccessDeniedBusinessException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                exchange,
                null
        );
    }


    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange,
            Map<String, String> errors
    ) {
        ApiError response = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                errors
        );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}