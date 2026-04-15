package com.bbplay.app.controller.user;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.RecentPlayItem;
import com.bbplay.app.dto.RecentPlayRequest;
import com.bbplay.app.interceptor.UserIdentityInterceptor;
import com.bbplay.app.service.RecentPlayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户最近播放控制器
 */
@RestController
@RequestMapping("/api/user/recent-play")
@RequiredArgsConstructor
public class UserRecentPlayController {

    private final RecentPlayService recentPlayService;

    /**
     * 写入最近播放记录
     */
    @PostMapping
    public ApiResponse<Void> recordPlay(@Valid @RequestBody RecentPlayRequest request, HttpServletRequest httpRequest) {
        String uid = (String) httpRequest.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        recentPlayService.recordPlay(uid, request.getResourceId(), request.getDurationSec(), request.getPositionSec());
        return ApiResponse.success();
    }

    /**
     * 读取最近播放列表
     */
    @GetMapping
    public ApiResponse<List<RecentPlayItem>> getRecentPlays(
            @RequestParam(defaultValue = "30") int limit,
            HttpServletRequest httpRequest) {
        String uid = (String) httpRequest.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        List<RecentPlayItem> recentPlays = recentPlayService.getRecentPlays(uid, limit);
        return ApiResponse.success(recentPlays);
    }
}
