package com.bbplay.app.service.impl;

import com.bbplay.app.constant.RedisKeyConstants;
import com.bbplay.app.constant.RedisTtlConstants;
import com.bbplay.app.dto.AudioPlayerState;
import com.bbplay.app.dto.AudioPlayerStateRequest;
import com.bbplay.app.service.AudioPlayerStateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 音频播放器状态服务实现
 */
@Service
@RequiredArgsConstructor
public class AudioPlayerStateServiceImpl implements AudioPlayerStateService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveState(String uid, AudioPlayerStateRequest request) {
        // 构建播放器状态
        AudioPlayerState state = new AudioPlayerState();
        state.setPlaylist(request.getPlaylist());
        state.setCurrentIndex(request.getCurrentIndex());
        state.setCurrentResourceId(request.getCurrentResourceId());
        state.setPlayMode(request.getPlayMode());
        state.setCurrentTimeSec(request.getCurrentTimeSec());
        state.setDurationSec(request.getDurationSec());
        state.setShuffleBag(request.getShuffleBag());
        state.setUpdatedAt(LocalDateTime.now());

        String key = RedisKeyConstants.buildAudioStateKey(uid);

        try {
            // 序列化为 JSON 并保存到 Redis
            String stateJson = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(key, stateJson);

            // 设置 TTL 30 天
            redisTemplate.expire(key, RedisTtlConstants.AUDIO_STATE_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化播放器状态失败", e);
        }
    }

    @Override
    public AudioPlayerState getState(String uid) {
        String key = RedisKeyConstants.buildAudioStateKey(uid);
        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return null;
        }

        try {
            // 反序列化 JSON
            String stateJson = value.toString();
            AudioPlayerState state = objectMapper.readValue(stateJson, AudioPlayerState.class);

            // 续期 30 天
            redisTemplate.expire(key, RedisTtlConstants.AUDIO_STATE_TTL);

            return state;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("反序列化播放器状态失败", e);
        }
    }

    @Override
    public void clearState(String uid) {
        String key = RedisKeyConstants.buildAudioStateKey(uid);
        redisTemplate.delete(key);
    }
}
