package com.it_goes.api.util.exception;

import org.springframework.http.HttpStatus;

public class AccountRequestException extends RuntimeException {
    private final HttpStatus status;

    public AccountRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
