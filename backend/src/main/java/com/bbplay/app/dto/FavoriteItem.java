package com.bbplay.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏项 DTO
 */
@Data
public class FavoriteItem {

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
     * 收藏时间
     */
    private LocalDateTime updatedAt;
}
