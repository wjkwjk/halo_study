package com.wjk.halo.exception;

import com.wjk.halo.exception.AbstractHaloException;
import org.springframework.http.HttpStatus;

public class ServiceException extends AbstractHaloException {
    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause){
        super(message, cause);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


}
