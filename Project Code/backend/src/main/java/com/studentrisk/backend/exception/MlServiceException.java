package com.studentrisk.backend.exception;

import org.springframework.http.HttpStatus;

public class MlServiceException extends RuntimeException {

    private final HttpStatus status;

    public MlServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
