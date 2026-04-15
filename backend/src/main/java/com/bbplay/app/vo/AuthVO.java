package com.bbplay.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 认证响应数据。
 */
@Data
@AllArgsConstructor
public class AuthVO {
    private String token;
    private String username;
    private String role;
}
