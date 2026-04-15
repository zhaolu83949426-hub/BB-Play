package com.bbplay.app.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 绘本页面更新请求
 */
@Data
public class PictureBookPageUpdateRequest {

    @NotNull(message = "页码不能为空")
    private Integer pageNumber;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    private String textContent;

    private String audioUrl;

    private Integer durationSec;
}
