package com.wjk.halo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public abstract class AbstractHaloException extends RuntimeException{

    private Object errorData;

    public AbstractHaloException(String message){
        super(message);
    }

    public AbstractHaloException(String message, Throwable cause) {
        super(message, cause);
    }

    @NonNull
    public AbstractHaloException setErrorData(@Nullable Object errorData){
        this.errorData = errorData;
        return this;
    }

    @NonNull
    public abstract HttpStatus getStatus();

    @Nullable
    public Object getErrorData(){
        return errorData;
    }

}
