package com.bbplay.app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户身份管理服务接口
 */
public interface UserIdentityService {

    /**
     * 获取或创建用户 uid
     * 从 Cookie 中读取 BBPLAY_UID，如果不存在则生成新的 uid 并写入 Cookie
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @return 用户 uid
     */
    String getOrCreateUid(HttpServletRequest request, HttpServletResponse response);

    /**
     * 刷新 uid 的过期时间
     * 访问时续期 180 天
     *
     * @param uid 用户 uid
     */
    void refreshUidExpiry(String uid);
}
