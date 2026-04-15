package com.bbplay.app.service.impl;

import com.bbplay.app.constant.RedisKeyConstants;
import com.bbplay.app.constant.RedisTtlConstants;
import com.bbplay.app.service.UserIdentityService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户身份管理服务实现
 */
@Service
public class UserIdentityServiceImpl implements UserIdentityService {

    private static final String COOKIE_NAME = "BBPLAY_UID";
    private static final int COOKIE_MAX_AGE = 180 * 24 * 60 * 60; // 180 天（秒）

    private final RedisTemplate<String, Object> redisTemplate;

    public UserIdentityServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String getOrCreateUid(HttpServletRequest request, HttpServletResponse response) {
        // 从 Cookie 中读取 uid
        String uid = extractUidFromCookie(request);

        if (uid == null || uid.isEmpty()) {
            // 生成新的 uid
            uid = UUID.randomUUID().toString();

            // 写入 Cookie（HttpOnly）
            Cookie cookie = new Cookie(COOKIE_NAME, uid);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(COOKIE_MAX_AGE);
            response.addCookie(cookie);

            // 初始化 Redis 用户数据
            String uidKey = RedisKeyConstants.buildUidKey(uid);
            LocalDateTime now = LocalDateTime.now();
            redisTemplate.opsForHash().put(uidKey, "createdAt", now.toString());
            redisTemplate.opsForHash().put(uidKey, "lastSeenAt", now.toString());
            redisTemplate.expire(uidKey, RedisTtlConstants.UID_TTL);
        } else {
            // 刷新过期时间
            refreshUidExpiry(uid);
        }

        return uid;
    }

    @Override
    public void refreshUidExpiry(String uid) {
        String uidKey = RedisKeyConstants.buildUidKey(uid);

        // 更新最后访问时间
        redisTemplate.opsForHash().put(uidKey, "lastSeenAt", LocalDateTime.now().toString());

        // 续期 180 天
        redisTemplate.expire(uidKey, RedisTtlConstants.UID_TTL);
    }

    /**
     * 从 Cookie 中提取 uid
     */
    private String extractUidFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
