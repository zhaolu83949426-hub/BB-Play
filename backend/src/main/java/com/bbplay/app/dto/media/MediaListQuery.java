package com.bbplay.app.dto.media;

import com.bbplay.app.common.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 前台资源列表查询参数。
 */
@Data
public class MediaListQuery {

    @Size(max = ValidationConstants.LEN_TITLE, message = "关键词长度不能超过100")
    private String keyword;
    private String mediaType;
    private String ageRange;
    private Long seriesId;
    private String sortBy;
    private String order;

    @Min(value = 1, message = "page最小为1")
    private Integer page = ValidationConstants.DEFAULT_PAGE;

    @Min(value = 1, message = "pageSize最小为1")
    @Max(value = 50, message = "pageSize最大为50")
    private Integer pageSize = ValidationConstants.DEFAULT_PAGE_SIZE;
}
