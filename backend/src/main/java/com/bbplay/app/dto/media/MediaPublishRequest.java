package com.bbplay.app.dto.media;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 上下架操作请求。
 */
@Data
public class MediaPublishRequest {

    @NotNull(message = "发布状态不能为空")
    private Boolean published;
}
