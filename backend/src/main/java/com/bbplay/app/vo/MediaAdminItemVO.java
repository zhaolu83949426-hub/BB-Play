package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端资源列表项。
 */
@Data
public class MediaAdminItemVO {

    private Long id;
    private String title;
    private String nickname;
    private String mediaType;
    private String series;
    private String ageRange;
    private Boolean isPublished;
    private Boolean isAbnormal;
    private LocalDateTime createdAt;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
