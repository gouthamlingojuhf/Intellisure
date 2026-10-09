package com.intellisure.workflownotificationservice.exception;

public class AccessDeniedBusinessException extends RuntimeException {
    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
