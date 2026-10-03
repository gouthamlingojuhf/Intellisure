package com.intellisure.quotepolicyservice.exception;

public class DownstreamServiceUnavailableException
        extends RuntimeException {

    public DownstreamServiceUnavailableException(
            String message
    ) {
        super(message);
    }

    public DownstreamServiceUnavailableException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}