package com.wjk.halo.exception;

import org.springframework.http.HttpStatus;

public class BeanUtilsException extends AbstractHaloException{
    public BeanUtilsException(String message) {
        super(message);
    }

    public BeanUtilsException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}