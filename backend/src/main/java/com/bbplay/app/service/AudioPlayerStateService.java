package com.bbplay.app.service;

import com.bbplay.app.dto.AudioPlayerState;
import com.bbplay.app.dto.AudioPlayerStateRequest;

/**
 * 音频播放器状态服务接口
 */
public interface AudioPlayerStateService {

    /**
     * 保存音频播放器状态
     *
     * @param uid     用户 uid
     * @param request 播放器状态请求
     */
    void saveState(String uid, AudioPlayerStateRequest request);

    /**
     * 获取音频播放器状态
     *
     * @param uid 用户 uid
     * @return 播放器状态，不存在返回 null
     */
    AudioPlayerState getState(String uid);

    /**
     * 清除音频播放器状态
     *
     * @param uid 用户 uid
     */
    void clearState(String uid);
}
