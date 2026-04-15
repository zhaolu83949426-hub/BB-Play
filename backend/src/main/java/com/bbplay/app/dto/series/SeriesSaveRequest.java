package com.bbplay.app.dto.series;

import com.bbplay.app.common.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系列新增编辑请求。
 */
@Data
public class SeriesSaveRequest {

    @NotBlank(message = "系列名称不能为空")
    @Size(max = ValidationConstants.LEN_SERIES_NAME, message = "系列名称长度不能超过50")
    private String name;

    @Size(max = ValidationConstants.LEN_SERIES_ALIAS, message = "系列别名长度不能超过50")
    private String alias;

    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;

    @NotNull(message = "排序权重不能为空")
    @Min(value = 0, message = "排序权重最小为0")
    @Max(value = 9999, message = "排序权重最大为9999")
    private Integer sortWeight;
}
