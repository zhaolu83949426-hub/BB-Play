package com.bbplay.app.service;

import com.bbplay.app.dto.LinkTestResult;

/**
 * 资源链接测试服务接口
 */
public interface ResourceLinkTestService {

    /**
     * 测试资源链接可用性
     * 优先使用 HEAD 请求，不支持则回退到 GET + Range
     * 超时控制：连接 3 秒，读取 5 秒
     * 判定规则：2xx/3xx 可访问，其余不可访问
     *
     * @param resourceId 资源 ID
     * @return 测试结果
     */
    LinkTestResult testLink(Long resourceId);
}
