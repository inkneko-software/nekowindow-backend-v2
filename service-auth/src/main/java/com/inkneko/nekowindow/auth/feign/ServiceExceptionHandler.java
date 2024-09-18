package com.inkneko.nekowindow.auth.feign;

//import com.inkneko.nekowindow.common.Response;
//import com.inkneko.nekowindow.common.ServiceException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ServiceExceptionHandler {

//    @ExceptionHandler(ServiceException.class)
//    @ResponseBody
//    public Response<?> serviceExceptionHandler(ServiceException e, HttpServletResponse response){
//        response.setStatus(e.getErrorCode());
//        return new Response<>(e.getErrorCode(), e.getMessage());
//    }
}
