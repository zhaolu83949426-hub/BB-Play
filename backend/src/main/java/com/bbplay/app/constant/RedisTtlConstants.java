package com.bbplay.app.constant;

import java.time.Duration;

/**
 * Redis TTL 常量定义
 */
public class RedisTtlConstants {

    /**
     * 用户标识 TTL：180 天
     */
    public static final Duration UID_TTL = Duration.ofDays(180);

    /**
     * 收藏数据 TTL：180 天
     */
    public static final Duration FAV_TTL = Duration.ofDays(180);

    /**
     * 最近播放 TTL：180 天
     */
    public static final Duration RECENT_TTL = Duration.ofDays(180);

    /**
     * 音频播放器状态 TTL：30 天
     */
    public static final Duration AUDIO_STATE_TTL = Duration.ofDays(30);
}
