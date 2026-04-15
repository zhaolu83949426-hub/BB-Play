package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 前台资源列表项。
 */
@Data
public class MediaFrontItemVO {

    private Long id;
    private String title;
    private String nickname;
    private String alias;
    private String series;
    private String ageRange;
    private String mediaType;
    private String playUrl;
    private String coverUrl;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
