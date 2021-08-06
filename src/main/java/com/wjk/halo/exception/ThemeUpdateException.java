package com.wjk.halo.exception;

public class ThemeUpdateException extends ServiceException {

    public ThemeUpdateException(String message) {
        super(message);
    }

    public ThemeUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
