package com.bbplay.app.service.impl;

import com.bbplay.app.constant.RedisKeyConstants;
import com.bbplay.app.constant.RedisTtlConstants;
import com.bbplay.app.dto.RecentPlayItem;
import com.bbplay.app.entity.MediaResource;
import com.bbplay.app.mapper.MediaResourceMapper;
import com.bbplay.app.service.RecentPlayService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 最近播放服务实现
 */
@Service
@RequiredArgsConstructor
public class RecentPlayServiceImpl implements RecentPlayService {

    private static final int MAX_RECENT_PLAY_COUNT = 30;

    private final RedisTemplate<String, Object> redisTemplate;
    private final MediaResourceMapper mediaResourceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void recordPlay(String uid, Long resourceId, Integer durationSec, Integer positionSec) {
        // 查询资源信息
        MediaResource resource = mediaResourceMapper.selectById(resourceId);
        if (resource == null) {
            throw new IllegalArgumentException("资源不存在");
        }

        // 构建播放记录项
        RecentPlayItem item = new RecentPlayItem();
        item.setResourceId(resourceId);
        item.setResourceType(resource.getMediaType());
        item.setTitle(resource.getTitle());
        item.setCoverUrl(resource.getCoverUrl());
        item.setPlayedAt(LocalDateTime.now());
        item.setDurationSec(durationSec);
        item.setPositionSec(positionSec);

        // 当前时间戳作为 score
        long timestamp = System.currentTimeMillis();

        String zsetKey = RedisKeyConstants.buildRecentZsetKey(uid);
        String metaKey = RedisKeyConstants.buildRecentMetaKey(uid);

        try {
            // ZADD：添加或更新索引
            redisTemplate.opsForZSet().add(zsetKey, resourceId.toString(), timestamp);

            // HSET：保存快照
            String itemJson = objectMapper.writeValueAsString(item);
            redisTemplate.opsForHash().put(metaKey, resourceId.toString(), itemJson);

            // 裁剪超限记录：保留最新 30 条
            Long count = redisTemplate.opsForZSet().zCard(zsetKey);
            if (count != null && count > MAX_RECENT_PLAY_COUNT) {
                // 获取需要删除的 resourceId
                Set<Object> toRemove = redisTemplate.opsForZSet().range(zsetKey, 0, count - MAX_RECENT_PLAY_COUNT - 1);
                if (toRemove != null && !toRemove.isEmpty()) {
                    // 从 ZSet 中删除
                    redisTemplate.opsForZSet().remove(zsetKey, toRemove.toArray());
                    // 从 Hash 中删除
                    redisTemplate.opsForHash().delete(metaKey, toRemove.toArray());
                }
            }

            // 续期 180 天
            redisTemplate.expire(zsetKey, RedisTtlConstants.RECENT_TTL);
            redisTemplate.expire(metaKey, RedisTtlConstants.RECENT_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化播放记录失败", e);
        }
    }

    @Override
    public List<RecentPlayItem> getRecentPlays(String uid, int limit) {
        String zsetKey = RedisKeyConstants.buildRecentZsetKey(uid);
        String metaKey = RedisKeyConstants.buildRecentMetaKey(uid);

        // ZREVRANGE：按 score 倒序获取 resourceId
        Set<Object> resourceIds = redisTemplate.opsForZSet().reverseRange(zsetKey, 0, limit - 1);

        if (resourceIds == null || resourceIds.isEmpty()) {
            return new ArrayList<>();
        }

        // HMGET：批量获取快照
        List<Object> itemJsonList = redisTemplate.opsForHash().multiGet(metaKey, resourceIds);

        List<RecentPlayItem> result = new ArrayList<>();
        for (Object itemJson : itemJsonList) {
            if (itemJson != null) {
                try {
                    RecentPlayItem item = objectMapper.readValue(itemJson.toString(), RecentPlayItem.class);
                    result.add(item);
                } catch (JsonProcessingException e) {
                    // 跳过解析失败的项
                }
            }
        }

        // 续期
        redisTemplate.expire(zsetKey, RedisTtlConstants.RECENT_TTL);
        redisTemplate.expire(metaKey, RedisTtlConstants.RECENT_TTL);

        return result;
    }
}
