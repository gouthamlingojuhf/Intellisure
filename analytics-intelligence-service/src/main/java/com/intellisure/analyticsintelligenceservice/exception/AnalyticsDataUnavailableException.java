package com.intellisure.analyticsintelligenceservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AnalyticsDataUnavailableException extends RuntimeException {
    public AnalyticsDataUnavailableException(String message) {
        super(message);
    }
}
