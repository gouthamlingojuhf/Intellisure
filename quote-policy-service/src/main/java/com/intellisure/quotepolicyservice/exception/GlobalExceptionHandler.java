package com.intellisure.quotepolicyservice.exception;

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
    public ResponseEntity<ApiError> handleResourceNotFound(
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
    public ResponseEntity<ApiError> handleBusinessException(
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

    @ExceptionHandler(NoUnderwriterAvailableException.class)
    public ResponseEntity<ApiError> handleNoUnderwriterAvailable(
            NoUnderwriterAvailableException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exchange,
                null
        );
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(
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
    public ResponseEntity<ApiError> handleValidationException(
            WebExchangeBindException exception,
            ServerWebExchange exchange
    ) {
        Map<String, String> validationErrors =
                new LinkedHashMap<>();

        exception.getFieldErrors().forEach(fieldError ->
                validationErrors.put(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                )
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                exchange,
                validationErrors
        );
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiError> handleServerWebInputException(
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage() == null
                        ? "Invalid request value"
                        : exception.getMessage(),
                exchange,
                null
        );
    }

    /*
     * This must remain the only handler for Exception.class.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
            Exception exception,
            ServerWebExchange exchange
    ) {
        log.error(
                "Unexpected error while processing path: {}",
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

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange,
            Map<String, String> validationErrors
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                validationErrors
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    @ExceptionHandler(
            DownstreamServiceUnavailableException.class
    )
    public ResponseEntity<ApiError>
    handleDownstreamUnavailable(
            DownstreamServiceUnavailableException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                exchange,
                null
        );
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ApiError> handleDownstreamError(
            DownstreamServiceException exception,
            ServerWebExchange exchange
    ) {
        return buildResponse(
                HttpStatus.BAD_GATEWAY,
                exception.getMessage(),
                exchange,
                null
        );
    }
}