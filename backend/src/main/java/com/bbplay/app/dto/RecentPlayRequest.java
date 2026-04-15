package com.bbplay.app.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 最近播放记录请求 DTO
 */
@Data
public class RecentPlayRequest {

    /**
     * 资源 ID
     */
    @NotNull(message = "资源 ID 不能为空")
    private Long resourceId;

    /**
     * 总时长（秒）
     */
    @Min(value = 0, message = "总时长不能为负数")
    private Integer durationSec;

    /**
     * 播放进度（秒）
     */
    @Min(value = 0, message = "播放进度不能为负数")
    private Integer positionSec;
}
