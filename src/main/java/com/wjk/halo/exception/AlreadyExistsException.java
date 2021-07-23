package com.wjk.halo.exception;

public class AlreadyExistsException extends BadRequestException{
    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
