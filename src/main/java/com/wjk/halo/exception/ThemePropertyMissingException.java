package com.wjk.halo.exception;

public class ThemePropertyMissingException extends BadRequestException {

    public ThemePropertyMissingException(String message) {
        super(message);
    }

    public ThemePropertyMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
