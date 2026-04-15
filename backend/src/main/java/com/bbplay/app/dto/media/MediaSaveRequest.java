package com.bbplay.app.dto.media;

import com.bbplay.app.common.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增或编辑资源请求。
 */
@Data
public class MediaSaveRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = ValidationConstants.LEN_TITLE, message = "标题长度不能超过100")
    private String title;

    @Size(max = ValidationConstants.LEN_NICKNAME, message = "昵称长度不能超过50")
    private String nickname;

    @Size(max = ValidationConstants.LEN_ALIAS, message = "别名长度不能超过100")
    private String alias;

    @NotNull(message = "系列不能为空")
    private Long seriesId;

    @NotBlank(message = "年龄段不能为空")
    private String ageRange;

    @NotBlank(message = "资源类型不能为空")
    private String mediaType;

    @NotBlank(message = "资源链接不能为空")
    @Size(max = ValidationConstants.LEN_PLAY_URL, message = "资源链接长度不能超过500")
    private String playUrl;

    @NotBlank(message = "封面链接不能为空")
    @Size(max = ValidationConstants.LEN_COVER_URL, message = "封面链接长度不能超过500")
    private String coverUrl;

    @Size(max = ValidationConstants.LEN_DESCRIPTION, message = "简介长度不能超过500")
    private String description;

    @NotNull(message = "上架状态不能为空")
    private Boolean isPublished;

    @NotNull(message = "异常状态不能为空")
    private Boolean isAbnormal;

    @Size(max = ValidationConstants.LEN_ABNORMAL_REMARK, message = "异常原因长度不能超过200")
    private String abnormalRemark;

    @Size(max = ValidationConstants.LEN_SOURCE_REMARK, message = "来源备注长度不能超过200")
    private String sourceRemark;

    @NotNull(message = "排序权重不能为空")
    @Min(value = 0, message = "排序权重最小为0")
    @Max(value = 9999, message = "排序权重最大为9999")
    private Integer sortWeight;
}
