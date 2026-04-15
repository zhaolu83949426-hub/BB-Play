package com.bbplay.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 阅读进度项
 */
@Data
public class BookProgressItem {

    private Long bookId;
    private Integer currentPage;
    private String playMode;
    private Boolean locked;
    private LocalDateTime lastReadAt;
}
