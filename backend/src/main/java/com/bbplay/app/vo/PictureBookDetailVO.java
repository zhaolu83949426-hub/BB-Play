package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 绘本详情 VO
 */
@Data
public class PictureBookDetailVO {

    private Long id;
    private String title;
    private String nickname;
    private String alias;
    private String series;
    private Long seriesId;
    private String ageRange;
    private String coverUrl;
    private String description;
    private Integer sortWeight;
    private Integer pageCount;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private List<PictureBookPageVO> pages;
}
