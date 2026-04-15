package com.bbplay.app.controller.user;

import com.bbplay.app.common.ApiResponse;
import com.bbplay.app.dto.AudioPlayerState;
import com.bbplay.app.dto.AudioPlayerStateRequest;
import com.bbplay.app.interceptor.UserIdentityInterceptor;
import com.bbplay.app.service.AudioPlayerStateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 音频播放器状态控制器
 */
@RestController
@RequestMapping("/api/user/audio-player-state")
@RequiredArgsConstructor
public class UserAudioPlayerController {

    private final AudioPlayerStateService audioPlayerStateService;

    /**
     * 保存音频播放器状态
     */
    @PutMapping
    public ApiResponse<Void> saveState(@RequestBody AudioPlayerStateRequest request, HttpServletRequest httpRequest) {
        String uid = (String) httpRequest.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        audioPlayerStateService.saveState(uid, request);
        return ApiResponse.success();
    }

    /**
     * 获取音频播放器状态
     */
    @GetMapping
    public ApiResponse<AudioPlayerState> getState(HttpServletRequest httpRequest) {
        String uid = (String) httpRequest.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        AudioPlayerState state = audioPlayerStateService.getState(uid);
        return ApiResponse.success(state);
    }

    /**
     * 清除音频播放器状态
     */
    @DeleteMapping
    public ApiResponse<Void> clearState(HttpServletRequest httpRequest) {
        String uid = (String) httpRequest.getAttribute(UserIdentityInterceptor.UID_ATTRIBUTE);
        audioPlayerStateService.clearState(uid);
        return ApiResponse.success();
    }
}
