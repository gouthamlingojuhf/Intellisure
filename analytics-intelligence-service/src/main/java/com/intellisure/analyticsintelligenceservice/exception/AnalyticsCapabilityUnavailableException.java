package com.intellisure.analyticsintelligenceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
public class AnalyticsCapabilityUnavailableException extends RuntimeException {
    public AnalyticsCapabilityUnavailableException(String message) {
        super(message);
    }
}
