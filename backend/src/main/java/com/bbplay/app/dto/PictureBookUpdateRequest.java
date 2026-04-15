package com.bbplay.app.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 绘本更新请求
 */
@Data
public class PictureBookUpdateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String nickname;

    private String alias;

    @NotNull(message = "系列不能为空")
    private Long seriesId;

    @NotBlank(message = "年龄段不能为空")
    private String ageRange;

    @NotBlank(message = "封面不能为空")
    private String coverUrl;

    private String description;

    private Integer sortWeight;
}
