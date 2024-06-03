package com.inkneko.nekowindow.common;


public class ServiceException extends RuntimeException{
    private final Integer errorCode;

    public ServiceException(Integer errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
