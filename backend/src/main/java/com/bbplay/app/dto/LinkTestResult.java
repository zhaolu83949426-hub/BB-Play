package com.bbplay.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 链接测试结果 DTO
 */
@Data
public class LinkTestResult {

    /**
     * 测试状态（SUCCESS / FAILED）
     */
    private String status;

    /**
     * HTTP 状态码
     */
    private Integer statusCode;

    /**
     * 延迟（毫秒）
     */
    private Integer latencyMs;

    /**
     * 测试时间
     */
    private LocalDateTime checkedAt;

    /**
     * 错误信息
     */
    private String errorMessage;
}
