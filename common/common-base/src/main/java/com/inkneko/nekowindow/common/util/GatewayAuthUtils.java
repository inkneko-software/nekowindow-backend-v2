package com.inkneko.nekowindow.common.util;

import com.inkneko.nekowindow.common.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class GatewayAuthUtils {
    public static String HEADER_USER_ID = "x-spring-cloud-gateway-auth-userid";
    public static String HEADER_SESSION = "x-spring-cloud-gateway-auth-session";

    /**
     * 用于获取当前外部请求的发起者uid，该uid已在网关处完成鉴权。
     * <p>
     * 由于获取uid逻辑依赖于网关所设置的请求头，因此为了使本函数正常工作，请不要使服务可由外部网络环境直接进行调用
     * <p>
     * 内部服务间的调用不走网关，请不要使用本函数进行鉴权，应当直接在调用时指定相关uid
     *
     * @return 若用户已登录，返回uid
     * @throws ServiceException 若用户未登录，抛出本异常
     */
    public static Long auth() {
        ServletRequestAttributes requestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = requestAttributes.getRequest();
        }
        return auth(request);
    }

    /**
     * 功能与{@link GatewayAuthUtils#auth()}相同，但可指定在未登录情况下不抛出异常
     *
     * @param isThrowsException 是否在未登录情况下抛出异常
     * @return 若用户已登录，返回uid，否则返回null
     */
    public static Long auth(Boolean isThrowsException) {
        ServletRequestAttributes requestAttributes = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes());
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = requestAttributes.getRequest();
        }
        return auth(request, isThrowsException);
    }

    public static Long auth(HttpServletRequest request) {
        return auth(request, true);
    }

    public static Long auth(HttpServletRequest request, Boolean throwsException) {
        if (request != null) {
            String userId = request.getHeader(HEADER_USER_ID);
            if (userId != null) {
                try {
                    return Long.parseLong(userId);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (throwsException) {
            throw new ServiceException(403, "未获取到当前用户信息，请先登录");
        }
        return null;
    }
}
