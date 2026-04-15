package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端绘本列表项
 */
@Data
public class PictureBookAdminItemVO {

    private Long id;
    private String title;
    private String nickname;
    private String series;
    private String ageRange;
    private Integer pageCount;
    private Boolean isPublished;
    private Boolean isAbnormal;
    private LocalDateTime createdAt;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
