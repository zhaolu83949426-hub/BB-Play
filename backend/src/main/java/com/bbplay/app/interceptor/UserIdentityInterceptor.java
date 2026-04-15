package com.bbplay.app.interceptor;

import com.bbplay.app.service.UserIdentityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户身份拦截器
 * 自动识别用户 uid 并存入 Request Attribute
 */
@Component
public class UserIdentityInterceptor implements HandlerInterceptor {

    public static final String UID_ATTRIBUTE = "uid";

    private final UserIdentityService userIdentityService;

    public UserIdentityInterceptor(UserIdentityService userIdentityService) {
        this.userIdentityService = userIdentityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取或创建 uid
        String uid = userIdentityService.getOrCreateUid(request, response);

        // 存入 Request Attribute，供 Controller 使用
        request.setAttribute(UID_ATTRIBUTE, uid);

        return true;
    }
}
