package com.bbplay.app.vo;

import lombok.Data;

/**
 * 绘本页面 VO
 */
@Data
public class PictureBookPageVO {

    private Long id;
    private Long bookId;
    private Integer pageNumber;
    private String imageUrl;
    private String textContent;
    private String audioUrl;
    private Integer durationSec;
}
