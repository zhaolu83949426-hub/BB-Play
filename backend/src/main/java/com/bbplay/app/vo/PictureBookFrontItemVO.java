package com.bbplay.app.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 前台绘本列表项
 */
@Data
public class PictureBookFrontItemVO {

    private Long id;
    private String title;
    private String nickname;
    private String alias;
    private String series;
    private String ageRange;
    private String coverUrl;
    private Integer pageCount;
    private Long clickCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
}
