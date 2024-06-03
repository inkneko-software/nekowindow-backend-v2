package com.inkneko.nekowindow.autoconfig.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import jodd.util.ArraysUtil;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class ServiceExceptionHandler {

    /**
     * 业务异常的处理，转换为业务输出统一结构
     * @param e 业务异常
     * @param response HTTP响应
     * @return 业务输出
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Response<?> serviceExceptionHandler(ServiceException e, HttpServletResponse response){
        response.setStatus(e.getErrorCode());
        return new Response<>(e.getErrorCode(), e.getMessage());
    }

    /**
     * 对 @Validated 校验异常的处理，将其转换为业务输出统一结构
     * @param e 校验异常
     * @param response HTTP响应
     * @return 业务输出
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response<?> validationExceptionHandler(MethodArgumentNotValidException e, HttpServletResponse response){
        response.setStatus(e.getStatusCode().value());
        List<FieldError> fieldErrors = e.getFieldErrors();
        String errorMsg = fieldErrors.stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(", "));
        return new Response<>(e.getStatusCode().value(), errorMsg);
    }


}