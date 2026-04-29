package com.bbplay.app.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 媒体流代理服务，负责将外部资源转发给前端播放器。
 */
public interface MediaStreamService {

    /**
     * 按资源 ID 代理输出媒体流。
     *
     * @param mediaId 资源 ID
     * @param range   前端传入的 Range 头
     * @param response HTTP 响应
     * @throws IOException 网络或输出异常
     */
    void streamByMediaId(Long mediaId, String range, HttpServletResponse response) throws IOException;
}
