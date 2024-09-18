package com.inkneko.nekowindow.user.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ServiceExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Response<?> serviceExceptionHandler(ServiceException e){
        return new Response<>(e.getErrorCode(), e.getMessage());
    }
}
