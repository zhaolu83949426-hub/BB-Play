package com.bbplay.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bbplay.app.dto.LinkTestResult;
import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.service.ResourceLinkTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * 资源链接测试服务实现
 */
@Service
@RequiredArgsConstructor
public class ResourceLinkTestServiceImpl implements ResourceLinkTestService {

    private static final int CONNECT_TIMEOUT = 3000; // 连接超时 3 秒
    private static final int READ_TIMEOUT = 5000; // 读取超时 5 秒

    private final MediaResourceMapper mediaResourceMapper;

    @Override
    public LinkTestResult testLink(Long resourceId) {
        MediaResource resource = mediaResourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在");
        }

        LinkTestResult result = new LinkTestResult();
        result.setCheckedAt(LocalDateTime.now());

        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(resource.getPlayUrl());
            int responseCode = executeHead(url, resource.getPlayUrl());
            if (isHeadNotSupported(responseCode)) {
                responseCode = executeRangeGet(url, resource.getPlayUrl());
            }
            fillResultByCode(result, responseCode, startTime);
        } catch (IOException e) {
            long latency = System.currentTimeMillis() - startTime;
            result.setLatencyMs((int) latency);
            result.setStatus("FAILED");
            result.setErrorMessage(e.getMessage());
        }

        updateTestResult(resourceId, result);
        return result;
    }

    /**
     * 执行 HEAD 请求
     */
    private int executeHead(URL url, String referer) throws IOException {
        HttpURLConnection connection = createConnection(url, referer);
        connection.setRequestMethod("HEAD");
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    /**
     * 执行 GET + Range 请求
     */
    private int executeRangeGet(URL url, String referer) throws IOException {
        HttpURLConnection connection = createConnection(url, referer);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Range", "bytes=0-0");
        int responseCode = connection.getResponseCode();
        connection.disconnect();
        return responseCode;
    }

    /**
     * 创建连接并设置通用请求头
     */
    private HttpURLConnection createConnection(URL url, String referer) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Referer", referer);
        return connection;
    }

    /**
     * HEAD 不支持时回退 GET + Range
     */
    private boolean isHeadNotSupported(int responseCode) {
        return responseCode == HttpURLConnection.HTTP_BAD_METHOD || responseCode == HttpURLConnection.HTTP_NOT_IMPLEMENTED;
    }

    /**
     * 根据状态码填充测试结果
     */
    private void fillResultByCode(LinkTestResult result, int responseCode, long startTime) {
        long latency = System.currentTimeMillis() - startTime;
        result.setStatusCode(responseCode);
        result.setLatencyMs((int) latency);
        if (responseCode >= 200 && responseCode < 400) {
            result.setStatus("SUCCESS");
            return;
        }
        result.setStatus("FAILED");
        result.setErrorMessage("HTTP 状态码: " + responseCode);
    }

    /**
     * 更新资源最近一次链接测试结果
     */
    private void updateTestResult(Long resourceId, LinkTestResult result) {
        LambdaUpdateWrapper<MediaResource> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MediaResource::getId, resourceId)
                .set(MediaResource::getLastTestStatus, result.getStatus())
                .set(MediaResource::getLastTestCode, result.getStatusCode())
                .set(MediaResource::getLastTestLatencyMs, result.getLatencyMs())
                .set(MediaResource::getLastTestError, result.getErrorMessage())
                .set(MediaResource::getLastTestAt, result.getCheckedAt());
        mediaResourceMapper.update(null, updateWrapper);
    }
}
