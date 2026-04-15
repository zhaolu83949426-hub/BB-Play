package com.bbplay.app.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 分页返回结构。
 */
@Getter
@AllArgsConstructor
public class PageResult<T> {

    private long page;
    private long pageSize;
    private long total;
    private List<T> records;
}
