package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 资源详情返回对象。
 */
@Data
public class MediaDetailVO {

    private Long id;
    private String title;
    private String nickname;
    private String alias;
    private Long seriesId;
    private String series;
    private String ageRange;
    private String mediaType;
    private String playUrl;
    private String coverUrl;
    private String description;
    private Boolean isPublished;
    private Boolean isAbnormal;
    private String abnormalRemark;
    private String sourceRemark;
    private Integer sortWeight;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
