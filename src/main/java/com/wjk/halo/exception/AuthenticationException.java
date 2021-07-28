package com.wjk.halo.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends AbstractHaloException{
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNAUTHORIZED;
    }
}
