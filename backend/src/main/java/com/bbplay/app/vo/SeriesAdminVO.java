package com.bbplay.app.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端系列列表项。
 */
@Data
public class SeriesAdminVO {

    private Long id;
    private String name;
    private String alias;
    private Boolean isEnabled;
    private Integer sortWeight;
    private Long resourceCount;
    private LocalDateTime createdAt;
}
