package com.bbplay.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 最近播放项 DTO
 */
@Data
public class RecentPlayItem {

    /**
     * 资源 ID
     */
    private Long resourceId;

    /**
     * 资源类型（AUDIO / VIDEO）
     */
    private String resourceType;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 封面 URL
     */
    private String coverUrl;

    /**
     * 播放时间
     */
    private LocalDateTime playedAt;

    /**
     * 总时长（秒）
     */
    private Integer durationSec;

    /**
     * 播放进度（秒）
     */
    private Integer positionSec;
}
