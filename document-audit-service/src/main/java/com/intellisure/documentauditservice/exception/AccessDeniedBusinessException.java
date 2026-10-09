package com.intellisure.documentauditservice.exception;

public class AccessDeniedBusinessException extends RuntimeException {
    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
