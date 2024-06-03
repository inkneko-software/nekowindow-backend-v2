package com.inkneko.nekowindow.common.util;

import com.inkneko.nekowindow.common.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GatewayAuthUtils {
    public static String HEADER_USER_ID = "x-spring-cloud-gateway-auth-userid";
    public static String HEADER_SESSION = "x-spring-cloud-gateway-auth-session";

    public static Long auth(){
        ServletRequestAttributes requestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = null;
        if (requestAttributes != null){
            request = requestAttributes.getRequest();
        }
        return auth(request);
    }

    public static Long auth(HttpServletRequest request){
        return auth(request, true);
    }

    public static Long auth(HttpServletRequest request, Boolean throwException){
        if (request != null){
            String userId = request.getHeader(HEADER_USER_ID);
            if (userId != null){
                try {
                    return Long.parseLong(userId);
                }catch (NumberFormatException ignored){}
            }
        }

        if (throwException){
            throw new ServiceException(403, "当前未登录，未获取到用户信息");
        }
        return null;
    }
}
