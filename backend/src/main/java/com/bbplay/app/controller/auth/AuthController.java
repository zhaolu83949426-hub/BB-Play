package com.bbplay.app.controller.auth;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.auth.LoginRequest;
import com.bbplay.app.dto.auth.RegisterRequest;
import com.bbplay.app.service.UserService;
import com.bbplay.app.vo.AuthVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册。
     */
    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success();
    }

    /**
     * 用户登录。
     */
    @PostMapping("/login")
    public ApiResponse<AuthVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    /**
     * 验证Token。
     */
    @GetMapping("/validate")
    public ApiResponse<AuthVO> validate(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        return ApiResponse.success(userService.validateToken(token));
    }
}
