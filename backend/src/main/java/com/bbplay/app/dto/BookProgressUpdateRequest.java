package com.bbplay.app.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 阅读进度更新请求
 */
@Data
public class BookProgressUpdateRequest {

    @NotNull(message = "当前页码不能为空")
    private Integer currentPage;

    private String playMode;

    private Boolean locked;
}
