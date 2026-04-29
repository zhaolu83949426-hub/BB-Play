package com.bbplay.app.service.impl;

import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.exception.BusinessException;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.service.MediaStreamService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 媒体流代理实现，后端统一访问 WebDAV 资源，前端无需再输入账号密码。
 */
@Service
@RequiredArgsConstructor
public class MediaStreamServiceImpl implements MediaStreamService {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 30000;
    private static final int MAX_REDIRECT_COUNT = 5;

    @Value("${webdav.username:}")
    private String webdavUsername;

    @Value("${webdav.password:}")
    private String webdavPassword;

    private final MediaResourceMapper mediaResourceMapper;

    @Override
    public void streamByMediaId(Long mediaId, String range, HttpServletResponse response) throws IOException {
        MediaResource resource = requireFrontPlayableMedia(mediaId);
        ProxyConnection proxyConnection = openSourceConnection(resource.getPlayUrl(), range);
        HttpURLConnection connection = proxyConnection.connection();
        try {
            int statusCode = proxyConnection.statusCode();
            response.setStatus(statusCode);
            copyStreamHeaders(connection, response);
            try (InputStream input = resolveInputStream(connection, statusCode)) {
                if (input != null) {
                    StreamUtils.copy(input, response.getOutputStream());
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    /**
     * 校验前台可播放资源，仅允许已上架且未异常资源被代理访问。
     */
    private MediaResource requireFrontPlayableMedia(Long mediaId) {
        MediaResource resource = mediaResourceMapper.selectById(mediaId);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        if (!Boolean.TRUE.equals(resource.getIsPublished()) || Boolean.TRUE.equals(resource.getIsAbnormal())) {
            throw new BusinessException("资源不可用");
        }
        return resource;
    }

    /**
     * 建立源站连接，处理重定向并保持鉴权头不丢失。
     */
    private ProxyConnection openSourceConnection(String playUrl, String range) throws IOException {
        URL sourceUrl = new URL(playUrl);
        String sourceAuth = resolveAuthorization(sourceUrl);
        String configAuth = resolveConfigAuthorization();
        String[] authCandidates = buildAuthCandidates(sourceAuth, configAuth);
        ProxyConnection last = null;
        for (String authorization : authCandidates) {
            ProxyConnection current = openConnectionWithRedirect(sourceUrl, range, authorization);
            if (last != null) {
                last.connection().disconnect();
            }
            last = current;
            if (current.statusCode() != HttpURLConnection.HTTP_FORBIDDEN
                && current.statusCode() != HttpURLConnection.HTTP_UNAUTHORIZED) {
                return current;
            }
        }
        return last;
    }

    /**
     * 解析并生成 Basic 鉴权头，优先取 URL 自带账号密码。
     */
    private String resolveAuthorization(URL sourceUrl) {
        String userInfo = sourceUrl.getUserInfo();
        if (!StringUtils.hasText(userInfo)) {
            return null;
        }
        String encoded = Base64.getEncoder().encodeToString(userInfo.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String resolveConfigAuthorization() {
        if (!StringUtils.hasText(webdavUsername) || !StringUtils.hasText(webdavPassword)) {
            return null;
        }
        String plain = webdavUsername + ":" + webdavPassword;
        String encoded = Base64.getEncoder().encodeToString(plain.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String[] buildAuthCandidates(String sourceAuth, String configAuth) {
        if (StringUtils.hasText(sourceAuth) && StringUtils.hasText(configAuth) && !sourceAuth.equals(configAuth)) {
            return new String[]{sourceAuth, configAuth, null};
        }
        if (StringUtils.hasText(sourceAuth)) {
            return new String[]{sourceAuth, null};
        }
        if (StringUtils.hasText(configAuth)) {
            return new String[]{configAuth, null};
        }
        return new String[]{null};
    }

    private ProxyConnection openConnectionWithRedirect(URL sourceUrl, String range, String authorization) throws IOException {
        URL currentUrl = sourceUrl;
        HttpURLConnection connection = createConnection(currentUrl, range, authorization);
        int statusCode = connection.getResponseCode();
        int redirectCount = 0;
        while (isRedirect(statusCode) && redirectCount < MAX_REDIRECT_COUNT) {
            String location = connection.getHeaderField("Location");
            if (!StringUtils.hasText(location)) {
                break;
            }
            URL redirectUrl = new URL(currentUrl, location);
            connection.disconnect();
            currentUrl = redirectUrl;
            connection = createConnection(currentUrl, range, authorization);
            statusCode = connection.getResponseCode();
            redirectCount++;
        }
        return new ProxyConnection(connection, statusCode);
    }

    /**
     * 构建外部媒体连接并透传关键请求头。
     */
    private HttpURLConnection createConnection(URL sourceUrl, String range, String authorization) throws IOException {
        // 媒体源是内网 WebDAV 地址，必须直连，避免命中服务端环境代理后被错误拦截。
        HttpURLConnection connection = (HttpURLConnection) sourceUrl.openConnection(Proxy.NO_PROXY);
        connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
        connection.setReadTimeout(READ_TIMEOUT_MS);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "*/*");
        connection.setRequestProperty("Referer", buildReferer(sourceUrl));
        if (StringUtils.hasText(range)) {
            connection.setRequestProperty("Range", range);
        }
        if (StringUtils.hasText(authorization)) {
            connection.setRequestProperty("Authorization", authorization);
        }
        return connection;
    }

    private String buildReferer(URL sourceUrl) {
        int port = sourceUrl.getPort();
        if (port > 0 && port != sourceUrl.getDefaultPort()) {
            return sourceUrl.getProtocol() + "://" + sourceUrl.getHost() + ":" + port + "/";
        }
        return sourceUrl.getProtocol() + "://" + sourceUrl.getHost() + "/";
    }

    private boolean isRedirect(int statusCode) {
        return statusCode == HttpURLConnection.HTTP_MOVED_PERM
            || statusCode == HttpURLConnection.HTTP_MOVED_TEMP
            || statusCode == HttpURLConnection.HTTP_SEE_OTHER
            || statusCode == HttpURLConnection.HTTP_MULT_CHOICE
            || statusCode == 307
            || statusCode == 308;
    }

    /**
     * 透传音视频播放依赖的响应头，确保浏览器可断点续传与跳播。
     */
    private void copyStreamHeaders(HttpURLConnection connection, HttpServletResponse response) {
        copyHeaderIfPresent(connection, response, "Content-Type");
        copyHeaderIfPresent(connection, response, "Content-Length");
        copyHeaderIfPresent(connection, response, "Content-Range");
        copyHeaderIfPresent(connection, response, "Accept-Ranges");
        copyHeaderIfPresent(connection, response, "Cache-Control");
        copyHeaderIfPresent(connection, response, "ETag");
        copyHeaderIfPresent(connection, response, "Last-Modified");
    }

    private void copyHeaderIfPresent(HttpURLConnection connection, HttpServletResponse response, String headerName) {
        String value = connection.getHeaderField(headerName);
        if (StringUtils.hasText(value)) {
            response.setHeader(headerName, value);
        }
    }

    private InputStream resolveInputStream(HttpURLConnection connection, int statusCode) throws IOException {
        if (statusCode >= 400) {
            return connection.getErrorStream();
        }
        return connection.getInputStream();
    }

    private record ProxyConnection(HttpURLConnection connection, int statusCode) {
    }
}
