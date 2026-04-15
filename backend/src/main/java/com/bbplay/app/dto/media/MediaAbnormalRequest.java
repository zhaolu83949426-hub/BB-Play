package com.bbplay.app.dto.media;

import com.bbplay.app.common.ValidationConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 异常标记请求。
 */
@Data
public class MediaAbnormalRequest {

    @NotNull(message = "异常状态不能为空")
    private Boolean abnormal;

    @Size(max = ValidationConstants.LEN_ABNORMAL_REMARK, message = "异常原因长度不能超过200")
    private String abnormalRemark;
}
