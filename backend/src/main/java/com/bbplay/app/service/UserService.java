package com.bbplay.app.service;

import com.bbplay.app.dto.auth.LoginRequest;
import com.bbplay.app.dto.auth.RegisterRequest;
import com.bbplay.app.vo.AuthVO;

/**
 * 用户认证服务接口。
 */
public interface UserService {

    /**
     * 用户注册。
     */
    void register(RegisterRequest request);

    /**
     * 用户登录。
     */
    AuthVO login(LoginRequest request);

    /**
     * 验证Token并获取用户信息。
     */
    AuthVO validateToken(String token);
}
